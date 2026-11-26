# Project Plan

MVP de PagoFlex: App Fintech de recaudación y gestión de pagos en Android con Kotlin y Jetpack Compose Material 3.

Arquitectura y requerimientos:
1. Arquitectura: MVVM con StateFlow/MutableStateFlow, Navigation Compose (sealed class Screen), Room Database en 3FN.
2. Estructura de paquetes: com.example.fintech_pago (o adaptado a com.example.apppagoflex_grupo36):
   - data/local/entity/: RolEntity, UsuarioEntity, EmpresaClienteEntity, CompromisoEntity, PagoEntity
   - data/local/dao/: PagoFlexDao
   - data/local/: AppDatabase
   - navigation/: Screen, NavigationEvent, AppNavigation
   - viewmodel/: SessionViewModel, UsuarioHomeViewModel
   - ui/components/: ResumenCard, etc.
   - ui/screen/: RoleSelectorScreen, HomeUsuarioScreen, y pantallas para Agente y Ejecutivo Empresa
   - MainActivity.kt
3. Roles de usuario (RF-01):
   - Usuario Final (R-01): Consulta compromisos consolidados, paga (simulado), revisa comprobantes, porcentaje de cumplimiento.
   - Agente PagoFlex (R-02): Atiende y gestiona reportes de problemas de los usuarios.
   - Ejecutivo Empresa Cliente (R-03): Registra/anula compromisos y monitorea tasa de cobranza.
4. Reglas de negocio:
   - Cálculo dinámico (no guardar en DB por 3FN): Total a pagar (monto + recargo si está vencido), Porcentaje cumplimiento (últimos 12 meses), Tasa de cobranza por empresa.
   - Pagos simulados generando comprobante único (1 a 0..1 con compromiso). Pago ordenado (vencido más antiguo primero por empresa).
   - Validación RUT con Módulo 11 y ocultamiento parcial en UI (15.*.*-5).
   - No datos bancarios ni tarjetas reales.

## Project Brief

# Project Brief: PagoFlex MVP

## Project Overview
PagoFlex is an Android fintech application for payment collection and financial management. The app delivers a multi-role experience tailored for End Users, PagoFlex Support Agents, and Client Company Executives.

---

## Features
1. **Multi-Role Authentication & Security**: Role switching between End User, PagoFlex Agent, and Company Executive with Chilean RUT validation (Módulo 11 algorithm) and privacy data masking (`15.*.*-5`).
2. **Consolidated Payment & Digital Receipts**: End users can view active debt commitments, automatically calculate dynamic totals (base amount + overdue surcharges), execute ordered simulated payments (oldest overdue first), and receive unique transaction receipts.
3. **Agent Support & Ticket Management**: PagoFlex Agents can view, manage, and resolve payment issues and support reports submitted by end users.
4. **Executive Administration & Analytics**: Client Company Executives can register or cancel debt commitments and monitor metrics such as collection rates and 12-month compliance percentages.

---

## High-Level Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) with `StateFlow` / `MutableStateFlow`
- **Navigation**: Jetpack Navigation 3 (state-driven)
- **Adaptive Strategy**: Compose Material Adaptive library for multi-pane and responsive layouts
- **Local Database**: Room Database (Normalized 3NF schema: `RolEntity`, `UsuarioEntity`, `EmpresaClienteEntity`, `CompromisoEntity`, `PagoEntity`, `PagoFlexDao`)
- **Concurrency**: Kotlin Coroutines & Flow

## Implementation Steps
**Total Duration:** 13m 8s

### Task_1_DataAndDomainSetup: Set up Room Database entities, DAOs, Database class, seed mock data, RUT validation with Módulo 11 and masking utility, and dynamic surcharge calculation logic.
- **Status:** COMPLETED
- **Updates:** Entities (RolEntity, UsuarioEntity, EmpresaClienteEntity, CompromisoEntity, PagoEntity), PagoFlexDao, AppDatabase with Room database seeding, RUTUtils with Modulo 11 validation and masking, and CalculoUtils for dynamic total/surcharge/compliance/collection-rate calculations implemented and verified with passing unit tests and successful debug build.
- **Acceptance Criteria:**
  - Room Database entities and DAO created
  - Database seeding populates initial mock data
  - RUTUtils correctly validates Chilean RUT Módulo 11 and masks RUT format
  - Dynamic surcharge calculation logic produces correct base and overdue totals
- **Duration:** 8m 43s

### Task_2_AuthAndNavigation: Implement SessionViewModel, Navigation setup (AppNavigation, Screen sealed class), and RoleSelectorScreen to enable role switching among End User, PagoFlex Agent, and Client Executive.
- **Status:** COMPLETED
- **Updates:** SessionViewModel with StateFlow, Navigation setup using NavDisplay, RoleSelectorScreen with RUT validation/masking and role selection, HomeUsuarioScreen, HomeAgenteScreen, HomeEjecutivoScreen, and MainActivity integration implemented and verified with assembleDebug and unit tests passing.
- **Acceptance Criteria:**
  - RoleSelectorScreen allows switching between End User, Agent, and Executive roles
  - SessionViewModel manages active session state using StateFlow
  - Navigation seamlessly routes users based on selected role
- **Duration:** 4m 25s

### Task_3_RoleScreensAndPaymentFlow: Implement HomeUsuarioScreen, HomeAgenteScreen, HomeEjecutivoScreen with their respective ViewModels, and shared UI components (ResumenCard) for debt payments, ticket management, and executive analytics.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - End User can view debts, execute ordered payments (oldest first), and view digital receipts
  - PagoFlex Agent can view and resolve support reports and payment issues
  - Executive can register/cancel debt commitments and view compliance/collection analytics
  - UI components render correctly with Jetpack Compose Material 3
- **StartTime:** 2026-10-25 17:52:51 GMT-03:00

### Task_4_RunAndVerify: Run application and instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** PENDING
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - critic_agent verifies application stability and confirms alignment with user requirements

