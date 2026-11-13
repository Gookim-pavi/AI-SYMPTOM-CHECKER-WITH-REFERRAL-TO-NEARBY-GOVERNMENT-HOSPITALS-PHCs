"""Local launcher for the SSS Medical Assist Java application."""

from __future__ import annotations

import argparse
import shutil
import signal
import socket
import subprocess
import sys
import time
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parent
SOURCE_ROOT = PROJECT_ROOT / "src"
BIN_ROOT = PROJECT_ROOT / "bin"
MAIN_CLASS = "com.sanjeevani.App"


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


def ensure_port_available(port: int) -> None:
    try:
        with socket.create_connection(("127.0.0.1", port), timeout=0.25):
            raise RuntimeError(
                f"Port {port} is already in use. Start with --port <number>."
            )
    except ConnectionRefusedError:
        return
    except OSError:
        return


def main() -> int:
    args = parse_args()
    if not 1 <= args.port <= 65535:
        raise ValueError("--port must be between 1 and 65535.")

    java = find_executable("java")
    if not args.no_build:
        compile_sources(find_executable("javac"))

    ensure_port_available(args.port)

    app_class = BIN_ROOT / "com" / "sanjeevani" / "App.class"
    if not app_class.exists():
        raise RuntimeError(
            f"{app_class} does not exist. Run without --no-build first."
        )

    process = subprocess.Popen(
        [java, "-cp", str(BIN_ROOT), MAIN_CLASS, str(args.port)],
        cwd=PROJECT_ROOT,
    )
    try:
        wait_for_server(args.port, process)
    except RuntimeError:
        if process.poll() is None:
            process.terminate()
        raise

    app_url = f"http://localhost:{args.port}"
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
