# Gemini 水印去除器 (Android 版)

這是一個專為 Android 16 設計的批量去除 Gemini AI 圖片水印的應用程序。它基於 [gemini-watermark-remover](https://github.com/journey-ad/gemini-watermark-remover) 的數學算法實現。

## 功能特點
- **批量處理**：一次性選擇多張圖片進行去水印處理。
- **無損修復**：採用反向 Alpha 混合算法，精確還原被水印覆蓋的像素，而非 AI 填補。
- **本地運行**：所有處理均在手機本地完成，無需上傳服務器，保護隱私。
- **自動適配**：自動識別 48x48 和 96x96 兩種尺寸的水印。

## 核心算法
應用程序實現了以下數學公式來恢復原始像素：
`original = (watermarked - α * 255) / (1 - α)`

## 技術棧
- **語言**：Kotlin
- **UI**：Jetpack Compose
- **圖片加載**：Coil
- **異步處理**：Kotlin Coroutines

## 使用說明
1. 打開應用，點擊「選擇圖片」。
2. 從系統相冊中選擇一張或多張帶有 Gemini 水印的圖片。
3. 點擊「開始處理」。
4. 處理完成後，圖片將自動保存至相冊的 `Pictures/GeminiRemover` 目錄下。

## 注意事項
- 本工具僅針對 Gemini 生成的右下角半透明星形水印。
- 建議在 Android 16 及以上版本運行以獲得最佳兼容性。
