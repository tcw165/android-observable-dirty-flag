Observable DirtyFlag
===

[![CircleCI](https://circleci.com/gh/boyw165/android-observable-dirty-flag.svg?style=svg)](https://circleci.com/gh/boyw165/android-observable-dirty-flag)
[![Download](https://api.bintray.com/packages/boyw165/android/observable-dirty-flag/images/download.svg)](https://bintray.com/boyw165/android/observable-dirty-flag/_latestVersion)

A generic dirty flag which has observable output with isolated flag environment and is thread-safe.

Gradle
---

Add this into your dependencies block.

```
implementation 'io.useful:dirty-flag:x.x.x'
```

If you cannot find the package, add this to your gradle repository

```
maven {
    url 'https://dl.bintray.com/boyw165/android'
}
```

Usage
---

The observable dirty flag is fairly simple, where there are `markDirty`, `markNotDirty`, and `updated` methods. For example:

```
// Flag bit
companion oject {
    const val DIRTY_A = 1.shl(0)
    const val DIRTY_B = 1.shl(1)
    const val DIRTY_C = 1.shl(2)
}
```

```
private val dirtyFlag = DirtyFlag(0)

// Observer #1 cares DIRTY_A and DIRTY_B only
dirtyFlag
    .updated(DIRTY_A, DIRTY_B)
    .subscribe { event ->
        println("observer #1 gets type, ${event.changedType}, updated and flag snapshot is ${event.flag}")
    }

// Observer #2 cares DIRTY_C
dirtyFlag
    .updated(DIRTY_C)
    .subscribe { event ->
        println("observer #2 gets type, ${event.changedType}, updated and flag snapshot is ${event.flag}")
    }

// Mark dirty
dirtyFlag.markDirty(DIRTY_A, DIRTY_B)
dirtyFlag.markDirty(DIRTY_C)

// Mark not dirty
dirtyFlag.markNotDirty(DIRTY_B)
dirtyFlag.markNotDirty(DIRTY_C)
```

Then you'll get the print log like:

```
// Log from initial subscription
observer #1 gets type, DirtyFlag.NO_CHANGE, updated and flag snapshot is 0
observer #2 gets type, DirtyFlag.NO_CHANGE, updated and flag snapshot is 0

// Log from markDirty()
observer #1 gets type, DIRTY_A, updated and flag snapshot is DIRTY_A
observer #1 gets type, DIRTY_B, updated and flag snapshot is DIRTY_A | DIRTY_B
observer #2 gets type, DIRTY_C, updated and flag snapshot is DIRTY_C

// Log from markNotDirty()
observer #1 gets type, DIRTY_B, updated and flag snapshot is DIRTY_A
observer #2 gets type, DIRTY_C, updated and flag snapshot is 0
```

You might notice that `observer #1` and `observer #2` gets isolated flag snapshot where one only cares about `DIRTY_A` and `DIRTY_B`, the other cares about `DIRTY_C`.

### With types constraint

If you want to constraint the `Int` types, you could use `@IntDef` annotation. For example:

```
class ExampleDirtyFlag(@Type override var flag: Int)
    : DirtyFlag(flag) {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(DIRTY_A,
            DIRTY_B,
            DIRTY_C)
    annotation class Type

    companion object {
        const val DIRTY_A = 1.shl(0)
        const val DIRTY_B = 1.shl(1)
        const val DIRTY_C = 1.shl(2)
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
```

To get more insights, you could look into the [DirtyFlagTest](dirtyflag/src/test/java/io/useful/dirtyflag/DirtyFlagTest.kt) code.
