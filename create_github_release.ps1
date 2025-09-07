# GitHub Release Creator для OGI TTS Android
# Запустите этот скрипт в папке M:\AI\Releases

Write-Host "🚀 Создание GitHub релиза OGI TTS Android v1.4" -ForegroundColor Green
Write-Host "=" * 50 -ForegroundColor Yellow

# Параметры релиза
$Version = "v1.4"
$Title = "OGI TTS Android v1.4 - Интеллектуальная фильтрация голосов"
$ApkFile = "OGI_TTS_Android_v1.4_2025-09-07.apk"
$NotesFile = "RELEASE_NOTES.md"
$RepoName = "ogi-tts-android-releases"  # Репозиторий только для релизов
$ProjectPath = "M:\AI\Releases"  # Работаем только с папкой Releases

# Проверка наличия файлов
Write-Host "📋 Проверка файлов..." -ForegroundColor Cyan
if (!(Test-Path $ApkFile)) {
    Write-Host "❌ Ошибка: APK файл не найден: $ApkFile" -ForegroundColor Red
    exit 1
}

if (!(Test-Path $NotesFile)) {
    Write-Host "❌ Ошибка: Файл описания не найден: $NotesFile" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Файлы найдены" -ForegroundColor Green

# Проверка наличия Git
Write-Host "🔍 Проверка Git..." -ForegroundColor Cyan
try {
    $gitVersion = git --version
    Write-Host "✅ Git найден: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка: Git не найден. Установите Git с https://git-scm.com/" -ForegroundColor Red
    exit 1
}

# Проверка наличия GitHub CLI
Write-Host "🔍 Проверка GitHub CLI..." -ForegroundColor Cyan
try {
    $ghVersion = gh --version
    Write-Host "✅ GitHub CLI найден: $ghVersion" -ForegroundColor Green
} catch {
    Write-Host "⚠️  GitHub CLI не найден. Устанавливаем..." -ForegroundColor Yellow

    # Попытка установки через winget
    try {
        winget install --id GitHub.cli
        Write-Host "✅ GitHub CLI установлен" -ForegroundColor Green
    } catch {
        Write-Host "❌ Не удалось установить GitHub CLI автоматически" -ForegroundColor Red
        Write-Host "📥 Скачайте и установите вручную: https://cli.github.com/" -ForegroundColor Yellow
        exit 1
    }
}

# Проверка аутентификации GitHub
Write-Host "🔐 Проверка аутентификации GitHub..." -ForegroundColor Cyan
try {
    gh auth status | Out-Null
    Write-Host "✅ GitHub аутентифицирован" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Требуется аутентификация GitHub" -ForegroundColor Yellow
    Write-Host "🔑 Выполните: gh auth login" -ForegroundColor Cyan

    $choice = Read-Host "Выполнить аутентификацию сейчас? (y/n)"
    if ($choice -eq "y" -or $choice -eq "Y") {
        gh auth login
    } else {
        Write-Host "❌ Аутентификация отменена" -ForegroundColor Red
        exit 1
    }
}

# Переход в папку проекта для работы с Git
Write-Host "📂 Переход в папку проекта..." -ForegroundColor Cyan
Push-Location $ProjectPath

try {
    # Создание тега (если нужно)
    Write-Host "🏷️  Создание тега $Version..." -ForegroundColor Cyan
    try {
        git tag $Version 2>$null
        Write-Host "✅ Тег $Version создан локально" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Тег уже существует: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # Возврат в папку Releases для создания релиза
    Pop-Location

    # Создание релиза
    Write-Host "📦 Создание релиза..." -ForegroundColor Cyan
    try {
        $releaseCommand = "gh release create $Version --title `"$Title`" --notes-file $NotesFile --latest $ApkFile --repo Roflsockie/$RepoName"
        Invoke-Expression $releaseCommand

        Write-Host "✅ Релиз успешно создан!" -ForegroundColor Green
        Write-Host "🎉 Поздравляем! Релиз OGI TTS Android v1.4 опубликован" -ForegroundColor Green

        # Получение URL релиза
        Write-Host "🔗 Ссылка на релиз:" -ForegroundColor Cyan
        try {
            gh release view $Version --json url -q ".url" --repo Roflsockie/$RepoName
        } catch {
            Write-Host "💡 Проверьте релиз вручную на GitHub" -ForegroundColor Yellow
        }

    } catch {
        Write-Host "❌ Ошибка при создании релиза: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "💡 Попробуйте создать релиз вручную через веб-интерфейс GitHub" -ForegroundColor Yellow
        Write-Host "🔗 Инструкция: https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository" -ForegroundColor Cyan
        exit 1
    }

} finally {
    # Гарантированный возврат в исходную папку
    Pop-Location
}

Write-Host "`n📋 Следующие шаги:" -ForegroundColor Cyan
Write-Host "1. ✅ Проверьте релиз на GitHub" -ForegroundColor White
Write-Host "2. ✅ Скачайте APK для тестирования" -ForegroundColor White
Write-Host "3. ✅ Поделитесь ссылкой в сообществах" -ForegroundColor White
Write-Host "4. ✅ Соберите обратную связь от пользователей" -ForegroundColor White

Write-Host "`n🎯 Релиз готов к распространению!" -ForegroundColor Green</content>
<parameter name="filePath">M:\AI\Releases\create_github_release.ps1
