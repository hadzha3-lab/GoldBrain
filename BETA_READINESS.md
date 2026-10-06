# GoldBrain beta readiness

Первая beta GoldBrain работает local-only.

Обязательные инварианты CI:

- существующая галерея доступна только на чтение (`scripts/check-media-readonly.sh`);
- локальная база/индекс не попадают в Android backup (`scripts/check-local-data-privacy.sh`);
- runtime приложения не запрашивает сетевые разрешения и не содержит прямого HTTP/WebView/cloud-клиента (`scripts/check-offline-beta.sh`);
- unit-тесты покрывают чистый запуск без доступа, выдачу доступа, восстановление после убийства процесса и отзыв разрешения во время индексации;
- Android instrumentation smoke-тест проверяет запуск `MainActivity` без разрешений и показ read-only объяснения до системного запроса галереи;
- instrumentation APK обязательно компилируется в CI до lint и основной APK-сборки;
- Android lint и debug APK build должны пройти после всех проверок.

Сетевой `curl` в GitHub Actions используется только во время сборки, чтобы положить русскую OCR-модель в APK. Это не runtime-код приложения.

APK не считается beta-кандидатом, если нарушен любой из этих инвариантов.
