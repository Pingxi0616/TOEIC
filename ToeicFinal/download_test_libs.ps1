$ErrorActionPreference = "Stop"

$libs = @(
    @{
        Url  = "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
        Dest = "lib\junit-platform-console-standalone-1.10.2.jar"
        Name = "JUnit 5 Standalone"
    },
    @{
        Url  = "https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar"
        Dest = "lib\jacocoagent.jar"
        Name = "JaCoCo Agent"
    },
    @{
        Url  = "https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.12/org.jacoco.cli-0.8.12-nodeps.jar"
        Dest = "lib\jacococli.jar"
        Name = "JaCoCo CLI"
    }
)

Write-Host "Downloading test libraries..." -ForegroundColor Cyan

foreach ($lib in $libs) {
    if (Test-Path $lib.Dest) {
        Write-Host "  [SKIP] $($lib.Name) already exists" -ForegroundColor Yellow
    } else {
        Write-Host "  [GET]  $($lib.Name)..." -NoNewline
        try {
            Invoke-WebRequest -Uri $lib.Url -OutFile $lib.Dest -UseBasicParsing
            $size = (Get-Item $lib.Dest).Length / 1MB
            Write-Host (" OK ({0:F1} MB)" -f $size) -ForegroundColor Green
        } catch {
            Write-Host " FAILED: $_" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host ""
Write-Host "All libraries ready. Run tests with: .\run_tests.bat" -ForegroundColor Cyan
