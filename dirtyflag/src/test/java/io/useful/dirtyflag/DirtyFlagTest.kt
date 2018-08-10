// Copyright Jul 2018-present useful.io
//
// Author: boyw165@gmail.com
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package io.useful.dirtyflag

import android.support.annotation.IntDef
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DirtyFlagTest {

    @Test
    fun equality() {
        Assert.assertEquals(DirtyEvent(flag = 0,
                                       changedType = 0),
                            DirtyEvent(flag = 0,
                                       changedType = 0))
        Assert.assertNotEquals(DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH,
                                          changedType = 0),
                               DirtyEvent(flag = 0,
                                          changedType = 0))
    }

    @Test
    fun dirtyHash() {
        val tester = ExampleDirtyFlag(flag = ExampleDirtyFlag.DIRTY_HASH)

        Assert.assertTrue(tester.isDirty(ExampleDirtyFlag.DIRTY_HASH))

        tester.markNotDirty(ExampleDirtyFlag.DIRTY_HASH)

        Assert.assertFalse(tester.isDirty(ExampleDirtyFlag.DIRTY_HASH))
    }

    @Test
    fun dirtyOther() {
        val tester = ExampleDirtyFlag(flag = ExampleDirtyFlag.DIRTY_PATH)

        Assert.assertFalse(tester.isDirty(ExampleDirtyFlag.DIRTY_HASH))
    }

    @Test
    fun dirtyObservable() {
        val tester = ExampleDirtyFlag(flag = 0)
        val testObserver = tester
            .onUpdate()
            .test()

        tester.markDirty(ExampleDirtyFlag.DIRTY_HASH)
        tester.markNotDirty(ExampleDirtyFlag.DIRTY_HASH)

        testObserver.assertValues(DirtyEvent(flag = 0,
                                             changedType = DirtyFlag.NO_CHANGE),
                                  DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH,
                                             changedType = ExampleDirtyFlag.DIRTY_HASH),
                                  DirtyEvent(flag = 0,
                                             changedType = ExampleDirtyFlag.DIRTY_HASH))
    }

    @Test
    fun dirtyObservableByTypes() {
        val tester = ExampleDirtyFlag(flag = 0)
        val testObserver = tester
            .onUpdate(ExampleDirtyFlag.DIRTY_HASH,
                      ExampleDirtyFlag.DIRTY_PATH)
            .test()

        tester.markDirty(ExampleDirtyFlag.DIRTY_TRANSFORM)
        tester.markDirty(ExampleDirtyFlag.DIRTY_HASH)
        tester.markDirty(ExampleDirtyFlag.DIRTY_PATH)

        testObserver.assertValues(DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH,
                                             changedType = ExampleDirtyFlag.DIRTY_HASH),
                                  DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH.or(ExampleDirtyFlag.DIRTY_PATH),
                                             changedType = ExampleDirtyFlag.DIRTY_PATH))
    }

    @Test
    fun dirtyAsyncObservable() {
        val tester = ExampleDirtyFlag(flag = 0)
        val testScheduler = TestScheduler()
        val testObserver = tester
            .onUpdate()
            .observeOn(testScheduler)
            .test()

        tester.markDirty(ExampleDirtyFlag.DIRTY_HASH)
        tester.markNotDirty(ExampleDirtyFlag.DIRTY_HASH)
        testScheduler.triggerActions()

        testObserver.assertValues(DirtyEvent(flag = 0,
                                             changedType = DirtyFlag.NO_CHANGE),
                                  DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH,
                                             changedType = ExampleDirtyFlag.DIRTY_HASH),
                                  DirtyEvent(flag = 0,
                                             changedType = ExampleDirtyFlag.DIRTY_HASH))
    }

    class ExampleDirtyFlag(@Type override var flag: Int) : DirtyFlag(flag) {

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(DIRTY_HASH,
                DIRTY_PATH,
                DIRTY_TRANSFORM)
        annotation class Type

        companion object {
            const val DIRTY_HASH = 1.shl(0)
            const val DIRTY_PATH = 1.shl(1)
            const val DIRTY_TRANSFORM = 1.shl(2)
        }

        override fun markDirty(@Type vararg types: Int) {
            super.markDirty(*types)
        }

        override fun markNotDirty(@Type vararg types: Int) {
            super.markNotDirty(*types)
        }

        override fun onUpdate(@Type vararg withTypes: Int): Observable<DirtyEvent> {
            return super.onUpdate(*withTypes)
        }
    }
}

