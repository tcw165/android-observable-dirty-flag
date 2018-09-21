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
    fun `dirty path, should see hash not dirty`() {
        val tester = ExampleDirtyFlag(flag = ExampleDirtyFlag.DIRTY_PATH)

        Assert.assertFalse(tester.isDirty(ExampleDirtyFlag.DIRTY_HASH))
    }

    @Test
    fun `dirty observation`() {
        val candidate = ExampleDirtyFlag(flag = 0)
        val tester = candidate
            .updated()
            .test()

        candidate.markDirty(ExampleDirtyFlag.DIRTY_HASH)
        candidate.markNotDirty(ExampleDirtyFlag.DIRTY_HASH)

        tester.assertValues(DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH,
                                       changedType = ExampleDirtyFlag.DIRTY_HASH),
                            DirtyEvent(flag = 0,
                                       changedType = ExampleDirtyFlag.DIRTY_HASH))
    }

    @Test
    fun `dirty observation by types`() {
        val tester = ExampleDirtyFlag(flag = 0)
        val testObserver = tester
            .updated(ExampleDirtyFlag.DIRTY_HASH,
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
    fun `async dirty observation`() {
        val tester = ExampleDirtyFlag(flag = 0)
        val testScheduler = TestScheduler()
        val testObserver = tester
            .updated()
            .observeOn(testScheduler)
            .test()

        tester.markDirty(ExampleDirtyFlag.DIRTY_HASH)
        tester.markNotDirty(ExampleDirtyFlag.DIRTY_HASH)
        testScheduler.triggerActions()

        testObserver.assertValues(DirtyEvent(flag = ExampleDirtyFlag.DIRTY_HASH,
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

        override fun updated(@Type vararg withTypes: Int): Observable<DirtyEvent> {
            return super.updated(*withTypes)
        }
    }
}

