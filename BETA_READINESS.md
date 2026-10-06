# GoldBrain beta readiness

Первая beta GoldBrain работает local-only.

Обязательные инварианты CI:

- существующая галерея доступна только на чтение (`scripts/check-media-readonly.sh`);
- локальная база/индекс не попадают в Android backup (`scripts/check-local-data-privacy.sh`);
- runtime приложения не запрашивает сетевые разрешения и не содержит прямого HTTP/WebView/cloud-клиента (`scripts/check-offline-beta.sh`);
- быстрые ML Kit OCR и image-labeling модели входят в приложение и не требуют загрузки через Google Play Services; русский Tesseract OCR также входит в APK;
- unit-тесты покрывают чистый запуск без доступа, выдачу доступа, восстановление после убийства процесса и отзыв разрешения во время индексации;
- Android instrumentation smoke-тест проверяет запуск `MainActivity` без разрешений и показ read-only объяснения до системного запроса галереи;
- instrumentation APK обязательно компилируется в CI;
- тот же smoke-test реально запускается на чистом Android 35 x86_64 эмуляторе через `ReactiveCircus/android-emulator-runner@v2.38.0`;
- универсальный debug APK контролируется отдельным size-budget (`scripts/check-apk-size.sh`);
- Android lint и debug APK build должны пройти после всех проверок.

Сетевой `curl` в GitHub Actions используется только во время сборки, чтобы положить русскую OCR-модель в APK. Это не runtime-код приложения.

APK не считается beta-кандидатом, если нарушен любой из этих инвариантов.
