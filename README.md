# JTrac

This project is derived from JTrac 2.3.3 (https://jtrac.info). It is dedicated to providing a lightweight, highly compatible Q&A text record and tracking system with offline static archiving capabilities and an intuitive user interface, making it exceptionally well-suited for knowledge management, supported by attachments for complex workflows. Development of this project is driven by OpenSpec v1.12.0 specification processes and assisted by Antigravity for architectural refactoring and quality control.

---

## Quick Start Options

### Option 1: Pre-packaged Standalone Bundle (GitHub Releases)

Pre-built standalone distributions bundling Jetty 10 and JTrac are available on GitHub Releases:

1. Download the latest `jtrac-2.3.3-x.x.x.zip` from GitHub Releases.
2. Unzip the downloaded archive to your preferred directory.
3. Start the server:
   - **Windows**: Double-click or run `start-jtrac.bat`
   - **Linux / macOS**: Run `./start-jtrac.sh` (ensure execute permissions: `chmod +x *.sh`)
4. Stop the server:
   - **Windows**: Run `stop-jtrac.bat`
   - **Linux / macOS**: Run `./stop-jtrac.sh`
5. Open `http://localhost:8888/` in your browser (default credentials: `admin` / `admin`).

> [!TIP]
> The default port is `8888`. To change the HTTP port, refer to `changePortListener.html` or edit `jetty.http.port` in `start.ini`.

---

### Option 2: Run with Docker Hub Image (`inmethod/jtrac`)

A ready-to-run container image based on Eclipse Temurin 17, Jetty 12, and full multilingual fonts is published on Docker Hub:
**[https://hub.docker.com/r/inmethod/jtrac](https://hub.docker.com/r/inmethod/jtrac)**

Run directly with Docker:
```bash
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac inmethod/jtrac:latest
```

Access the application at `http://localhost:8888/`.

> [!NOTE]
> If you prefer to build the Docker image locally from source code, refer to the [`docker/README.md`](docker/README.md) guide.

---

## Documentation

All detailed project specifications, technical guides, and multilingual documentation are organized in the [`docs/`](docs/) directory:

- **Build & Compilation Guides (8 Languages)**: [`docs/build/BUILD_en.md`](docs/build/BUILD_en.md)
- **System Administrator Guides (8 Languages)**: [`docs/admin/ADMIN_GUIDE_en.md`](docs/admin/ADMIN_GUIDE_en.md)
- **Docker Deployment & Volume Guide**: [`docker/README.md`](docker/README.md)
- **Multilingual Detailed Documentation**: [`docs/`](docs/)
