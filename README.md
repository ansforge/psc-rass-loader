# pscload-v2
pscload V2
Component to load RASS data in psc db

## Developement

### Release procedure

Whenever a version is ready for release, run the following commands on the `main` branch (or on the maintenance branch if we're about to issue a production FIX). This should run on any shell, be it `bash`, `cmd` or if needed `gitbash`.

```bash
mvn release:prepare -DautoVersionSubmodules=true -DtagNameFormat=@{version}
git push
git push origin <new_version_tag>
```

where `<new_version_tag>` stands for the new version.

Eg to relase `1.0.1` :

```bash
mvn release:prepare -DautoVersionSubmodules=true -DtagNameFormat=@{version}
git push
git push origin 1.0.1
```
