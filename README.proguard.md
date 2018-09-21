Proguard Troubles Shooting
===

How to run ProGuard for DEBUG build?
---

Just append `-PtestProguard=true` to the Gradle command build, for example,

```
./gradlew clean assembleDebug -PtestProguard=true
```

Want to see the entire (merged) configuration?
---

Add this line to the proguard file:

```
# Specifies to write out the entire configuration that has been parsed, with
# included files and replaced variables.
-printconfiguration merged_proguard.pro
```

Where to check the proguard result?
---

With each build (under `<module-name>/build/outputs/mapping/`), ProGuard outputs the following files:

- `dump.txt`: Describes the internal structure of all the class files in the APK.
- `mapping.txt`: Provides a translation between the original and obfuscated class, method, and field names.
- `seeds.txt`: Lists the classes and members that were not obfuscated.
- `usage.txt`: Lists the code that was removed from the APK.


Appendix
---

- [android shrink-code](https://developer.android.com/studio/build/shrink-code)
- [troubles shooting](https://www.guardsquare.com/en/proguard/manual/troubleshooting#descriptorclass)

