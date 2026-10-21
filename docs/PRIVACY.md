# Robot source stays private

The community repository is a fresh history with no imported competition robot programs. Only simulator software, upstream examples/dependencies and newly written generic examples are distributed.

Local data is ignored by Git:
- ImportedTeamCode: source imported by the current user.
- build: compiled classes, compiler logs and staged imports.
- import-history: recoverable previous imports.
- profiles: local robot configurations.
- source ZIPs and import manifests.

Never force-add these paths. Git ignore rules do not revoke files already shared or make an existing repository's history private.

Use Export-Share.ps1 to generate a share ZIP. In a Git checkout it requires a clean committed working tree, including no staged changes. Review simulator changes before committing; never commit imported robot code or local calibration to make an export pass. Untracked files are not exported.

The export excludes Git history, ImportedTeamCode, build, out, artifacts, import-history, profiles, import manifests, compiled classes, ZIPs and logs, even if listed. The import folder receives only an empty placeholder. Paths are validated before output is created: absolute paths, traversal, duplicate names and linked files/directories are rejected. The ZIP's file manifest is regenerated without excluded filenames.

An extracted ZIP without Git uses .share-files.txt as its approved file list. This list is not a content-integrity signature: edits to approved files, including robot.properties, will be shared. Keep a pristine community download for redistribution and store personal robot settings in profiles. Avoid sending a ZIP of your live simulator folder.

Run `powershell -NoProfile -ExecutionPolicy Bypass -File tests/export-privacy.ps1` to check path rejection, private-file exclusions and clean Git export requirements. The junction test reports a skip when Windows or the sandbox prevents creating links.

The original private development repository remains private and separate. Do not change its visibility to public: older commits may contain robot code. Other teams should use the new community repository.

Java executes locally with desktop permissions. Only compile source you trust. The local UI sends selected source to the local Java process; no upload to GitHub happens during importing or compiling.
