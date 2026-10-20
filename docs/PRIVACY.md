# Robot source stays private

The community repository is a fresh history with no imported competition robot programs. Only simulator software, upstream examples/dependencies and newly written generic examples are distributed.

Local data is ignored by Git:
- ImportedTeamCode: source imported by the current user.
- build: compiled classes, compiler logs and staged imports.
- import-history: recoverable previous imports.
- profiles: local robot configurations.
- source ZIPs and import manifests.

Never force-add these paths. Git ignore rules do not revoke files already shared or make an existing repository's history private.

Use Export-Share.ps1 to generate a share ZIP from the filtered tracked file set. It excludes Git history and local data even after you import your own code. Avoid sending a ZIP of your live simulator folder.

The original private development repository remains private and separate. Do not change its visibility to public: older commits may contain robot code. Other teams should use the new community repository.

Java executes locally with desktop permissions. Only compile source you trust. The local UI sends selected source to the local Java process; no upload to GitHub happens during importing or compiling.
