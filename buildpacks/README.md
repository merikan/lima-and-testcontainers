# Buildpacks
This is [multi-arch-image-test](../multi-arch-image-test), but the image is built with [Cloud Native Buildpacks](https://buildpacks.io) through the Spring Boot Maven plugin instead of a Dockerfile.

* `spring-boot:build-image-no-fork` runs in the `package` phase with the default Paketo builder. Buildpacks build one platform per run, so there is one execution per platform, each with its own tag: `todo-buildpacks:1.0.0-SNAPSHOT-amd64` and `todo-buildpacks:1.0.0-SNAPSHOT-arm64`.
* The `native-amd64`, `native-x86_64` and `native-arm64` profiles activate on the JVM's `os.arch` and also tag the native image `todo-buildpacks:1.0.0-SNAPSHOT`. The JVM reports `amd64` on Linux and `x86_64` on Intel Macs, so amd64 needs two profiles.
* [ApplicationImageIT](src/test/java/com/merikan/testcontainers/todo/ApplicationImageIT.java) starts the native image together with MariaDB on a shared network and calls the REST API.

The builder isn't pinned. The plugin pulls `paketobuildpacks/builder-noble-java-tiny:latest` on every build, so the same commit can produce a different image later, with a newer JRE, run image or buildpacks. Paketo releases a new builder almost daily. For reproducible builds, pin a version in the `<image>` configuration of both executions, for example `<builder>paketobuildpacks/builder-noble-java-tiny:0.0.214</builder>`, and keep it updated with a tool such as Renovate.

## Lima setup
On Lima with rootless Docker, the VM needs two changes before the build works. Add both with `limactl edit <instance>`.

**Emulation for the non-native platform.** Buildpacks run the builder for the target platform, so the VM must be able to run its binaries. On Apple Silicon with the `vz` VM type, enable Rosetta:

```
$ limactl edit <instance> --rosetta --start
```

The `--rosetta` flag sets these keys in the instance config:

```yaml
vmOpts:
  vz:
    rosetta:
      enabled: true
      binfmt: true
```

On other hosts, install QEMU binfmt in the VM instead, for example the `qemu-user-static` package.

**A Docker socket at `/var/run/docker.sock`.** Spring Boot mounts that path into the builder container, but rootless Docker has its socket at `/run/user/<uid>/docker.sock`. With rootful Docker, such as Lima's `docker-rootful` template, the socket is already at `/var/run/docker.sock`, so this step isn't needed and only emulation is. This provision script installs a systemd-tmpfiles rule that creates the symlink early in boot, before rootless Docker starts:

```yaml
provision:
- mode: system
  # This script symlinks /var/run/docker.sock to the rootless Docker socket, for tools that
  # mount the default socket path into a container, such as Spring Boot's buildpacks builder.
  # It uses a systemd-tmpfiles rule so the symlink exists before rootless Docker starts and
  # copies /run. The rule takes effect from the second boot after it is added.
  script: |
    #!/bin/sh
    echo 'L+ /run/docker.sock - - - - /run/user/{{.UID}}/docker.sock' > /etc/tmpfiles.d/docker-sock.conf
```

The rule takes effect from the second boot after the change, since the provision script runs after systemd-tmpfiles on the first boot.

Steps to apply it:

1. Stop the instance and open its config in your editor. `limactl edit` without flags opens the instance's `lima.yaml` in `$EDITOR`:
   ```
   $ limactl stop <instance>
   $ limactl edit <instance>
   ```
2. Add the entry above to the `provision` list, next to any other `mode: system` entries, then save and close the editor.
3. Start the instance. This is the first boot, so the provision script writes the rule, but the symlink isn't created yet:
   ```
   $ limactl start <instance>
   ```
4. Restart the instance. On this second boot, systemd-tmpfiles creates the symlink before rootless Docker starts:
   ```
   $ limactl restart <instance>
   ```
   `limactl restart` only works on a running instance. `limactl stop <instance> && limactl start <instance>` does the same.

Both restarts stop all running containers.


## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
