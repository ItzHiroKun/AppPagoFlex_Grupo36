# AppPagoFlex - Grupo 36

Aplicación móvil nativa desarrollada en Kotlin y Jetpack Compose para la gestión de pagos, perfil de usuario y consulta de servicios financieros, conectada a microservicios propios y APIs externas, con pruebas unitarias automatizadas y build de producción firmado.

---

## 1. Nombre del Proyecto
**AppPagoFlex_Grupo36**

## 2. Integrantes
* **[Ignacio Andree Vera Sánchez]** — Desarrollo integral (Arquitectura MVVM, UI en Jetpack Compose, persistencia local con Room/DataStore, integración con Retrofit, pruebas unitarias con Kotest/MockK y despliegue de APK firmado).

---

## 3. Funcionalidades Principales
* **Gestión de Perfil y Cámara:** Visualización y actualización de foto de perfil desde la galería o captura directa con cámara mediante `FileProvider` y almacenamiento local persistente.
* **Preferencias de Tema (Modo Oscuro):** Activación y persistencia en tiempo real del modo oscuro utilizando `Jetpack DataStore` y `StateFlow`.
* **Persistencia Local:** Almacenamiento estructurado de datos en dispositivo mediante base de datos `Room`.
* **Consumo de Servicios Web:** Comunicación asíncrona mediante `Retrofit` con soporte para serialización JSON (`Gson` / `Moshi`) e interceptores HTTP (`OkHttp`).
* **Calidad y Cobertura de Código:** Suite de 41 pruebas unitarias automatizadas con **JUnit 5**, **Kotest**, **MockK** y **Coroutines Test**, alcanzando un **88% de cobertura de código** validado con **JaCoCo**.

---

## 4. Endpoints Utilizados

### Microservicio Propio (Spring Boot)
* **Base URL:** `http://localhost:8080/` *(conectado mediante túnel ADB `adb reverse tcp:8080 tcp:8080`)*
* `GET /api/compromisos` — Obtiene el listado completo de compromisos financieros registrados.
* `GET /api/compromisos/usuario/{rut}` — Consulta los compromisos asociados a un usuario específico mediante su RUT.
* `POST /api/compromisos` — Crea y registra un nuevo compromiso de pago.
* `PUT /api/compromisos/{id}/pagar` — Marca un compromiso existente como pagado según su ID.
* `DELETE /api/compromisos/{id}` — Elimina un compromiso registrado por su ID.

### API Externa (Mindicador.cl)
* **Base URL:** `https://mindicador.cl/`
* `GET /api` — Consulta en tiempo real los indicadores económicos de Chile (UF, Dólar, Euro, IPC, UTM, entre otros) para su visualización en la aplicación.

---

## 5. Pasos para Ejecutar el Proyecto

### Requisitos Previos
* **Android Studio** (versión reciente compatible con AGP 9 / Compile SDK 37).
* **JDK 11** o superior.
* Dispositivo físico Android (API 24+) o emulador configurado.
* Iniciar PRIMERO los microservicios, luego ejecutar el APK e ingresar en la web: http://localhost:8080/api/compromisos , para revisar si el POST de la sesión del ejecutivo se realiza correctamente en la base de datos
* Cliente Retrofit apunta a `localhost:8080`[cite: 5]:

    ```bash
    adb reverse tcp:8080 tcp:8080
    ```[cite: 5]