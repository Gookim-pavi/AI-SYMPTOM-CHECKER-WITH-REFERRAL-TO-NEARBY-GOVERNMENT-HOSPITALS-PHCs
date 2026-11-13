"""Local launcher for the SSS Medical Assist Java application."""

from __future__ import annotations

import argparse
import shutil
import signal
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parent
SOURCE_ROOT = PROJECT_ROOT / "src"
BIN_ROOT = PROJECT_ROOT / "bin"
MAIN_CLASS = "com.sanjeevani.App"
APP_CLASS = BIN_ROOT / "com" / "sanjeevani" / "App.class"


def find_executable(name: str) -> str:
    executable = shutil.which(name)
    if executable is None:
        raise RuntimeError(
            f"{name} was not found on PATH. Install a JDK and try again."
        )
    return executable


def compile_sources(javac: str) -> None:
    sources = sorted(SOURCE_ROOT.rglob("*.java"))
    if not sources:
        raise RuntimeError(f"No Java source files found in {SOURCE_ROOT}")

    BIN_ROOT.mkdir(exist_ok=True)
    command = [
        javac,
        "-d",
        str(BIN_ROOT),
        "-sourcepath",
        str(SOURCE_ROOT),
        *(str(source) for source in sources),
    ]
    print("Compiling Java sources...", flush=True)
    subprocess.run(command, cwd=PROJECT_ROOT, check=True)


def needs_compile() -> bool:
    if not APP_CLASS.exists():
        return True
    app_mtime = APP_CLASS.stat().st_mtime
    return any(source.stat().st_mtime > app_mtime for source in SOURCE_ROOT.rglob("*.java"))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Build and run SSS Medical Assist on localhost."
    )
    parser.add_argument(
        "--port",
        type=int,
        default=8080,
        help="Local port for the application (default: 8080).",
    )
    parser.add_argument(
        "--no-build",
        action="store_true",
        help="Run existing compiled classes without compiling first.",
    )
    return parser.parse_args()


def wait_for_server(port: int, process: subprocess.Popen[bytes]) -> None:
    deadline = time.monotonic() + 10
    while time.monotonic() < deadline:
        if process.poll() is not None:
            raise RuntimeError("The Java application stopped before the server started.")
        try:
            with socket.create_connection(("127.0.0.1", port), timeout=0.25):
                return
        except OSError:
            time.sleep(0.1)
    raise RuntimeError(f"The application did not start on port {port} within 10 seconds.")


def is_port_available(port: int) -> bool:
    probe = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        probe.bind(("127.0.0.1", port))
        return True
    except OSError:
        return False
    finally:
        probe.close()


def select_port(requested_port: int) -> int:
    for port in range(requested_port, min(requested_port + 10, 65536)):
        if is_port_available(port):
            if port != requested_port:
                print(
                    f"Port {requested_port} is busy; using available port {port}.",
                    flush=True,
                )
            return port
    raise RuntimeError(f"No available port found between {requested_port} and {requested_port + 9}.")


def existing_app_url(port: int) -> str | None:
    url = f"http://127.0.0.1:{port}/api/health"
    try:
        with urllib.request.urlopen(url, timeout=0.5) as response:
            if response.status == 200 and b"SSS Medical Assist" in response.read():
                return f"http://localhost:{port}"
    except (OSError, urllib.error.URLError):
        return None
    return None


def main() -> int:
    args = parse_args()
    if not 1 <= args.port <= 65535:
        raise ValueError("--port must be between 1 and 65535.")

    running_url = existing_app_url(args.port)
    if running_url:
        print("SSS Medical Assist is already running. Click this link to open it:", flush=True)
        print(running_url, flush=True)
        return 0

    java = find_executable("java")
    if not args.no_build and needs_compile():
        compile_sources(find_executable("javac"))
    elif not args.no_build:
        print("Using up-to-date compiled classes.", flush=True)

    port = select_port(args.port)

    if not APP_CLASS.exists():
        raise RuntimeError(
            f"{APP_CLASS} does not exist. Run without --no-build first."
        )

    process = subprocess.Popen(
        [java, "-cp", str(BIN_ROOT), MAIN_CLASS, str(port)],
        cwd=PROJECT_ROOT,
    )
    try:
        wait_for_server(port, process)
    except RuntimeError:
        if process.poll() is None:
            process.terminate()
        raise

    app_url = f"http://localhost:{port}"
    print("\nSSS Medical Assist is running. Click this link to open it:", flush=True)
    print(app_url, flush=True)

    def stop_process(signum: int, _frame: object) -> None:
        del signum
        if process.poll() is None:
            process.terminate()

    signal.signal(signal.SIGINT, stop_process)
    if hasattr(signal, "SIGTERM"):
        signal.signal(signal.SIGTERM, stop_process)

    try:
        return process.wait()
    except KeyboardInterrupt:
        stop_process(signal.SIGINT, None)
        return process.wait()


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (RuntimeError, ValueError, subprocess.CalledProcessError) as error:
        print(f"Error: {error}", file=sys.stderr)
        raise SystemExit(1)
