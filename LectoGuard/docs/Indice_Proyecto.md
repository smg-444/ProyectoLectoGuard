# Índice Proyecto Fin de Ciclo Formativo — LectoGuard Pro

Autor: Samy Mounaji Gouiss  
Ciclo: 2º DAM (Desarrollo de Aplicaciones Multiplataforma)  
Fecha: 26/10/2025  
Proyecto: LectoGuard Pro — Plataforma social para amantes de la lectura

---

## 1. Resumen Ejecutivo
LectoGuard Pro amplía la app base LectoGuard añadiendo funcionalidades sociales: perfiles, seguidores, chat en tiempo real, recomendaciones, valoraciones y reseñas, manteniendo sincronización offline/online y una experiencia fluida. La arquitectura propuesta es MVVM + Clean, con Room para local y Firebase (Auth, Firestore, Storage, FCM) para funcionalidades sociales y tiempo real.

---

## 2. Estado Actual del Proyecto (LectoGuard)
- Login y registro local (Room) con validaciones básicas.
- Gestión local de usuarios y libros (Room: `User`, `Book`, `UserBook`).
- Listado de libros (mock/API), detalle de libro y guardado sin duplicados.
- Vista de libros guardados por usuario.
- Pantallas implementadas: `Login`, `SignUp`, `Home`, `SaveBook`, `Profile`, `SavedBooks`, `HeaderFragment`.
- Integración de red: Retrofit sobre endpoint Firebase Realtime DB (lectura pública) para sinopsis/detalle.
- Modo offline/online básico (consulta local cuando no hay conectividad).
- Estructura Clean + MVVM y Repositories; uso de ViewBinding.

Tecnologías actuales:
- Kotlin, AndroidX, Room, Retrofit (Scalars + Gson), Glide, Coroutines, LiveData.
- Min SDK 24, Target SDK 35.

---

## 3. Brecha vs Anteproyecto (Qué falta por implementar)
- Autenticación: Falta Firebase Authentication (email/password y social opcional).
- Datos sociales: Falta Firestore (perfiles sociales, seguidores/seguidos, feed, valoraciones, reseñas, recomendaciones).
- Chat: Falta módulo completo de conversaciones y mensajes en tiempo real (Firestore) y notificaciones push (FCM).
- Recomendaciones: Falta lógica (basadas en intereses/histórico y recomendaciones directas entre usuarios).
- Valoraciones/Reseñas: Falta puntuación 1–5, reseñas textuales, “me gusta” y feed social.
- Gestión avanzada de biblioteca: Falta estados (leyendo, leído, abandonado…), etiquetas personalizadas y estadísticas de lectura.
- DI: Falta Dagger Hilt para inyección de dependencias.
- Navegación: Falta Navigation Component (ahora son Activities con BottomNavigation).
- UI/UX: Falta modo oscuro, accesibilidad reforzada, i18n en_US, búsqueda avanzada y filtros.
- Almacenamiento de medios: Falta Firebase Storage (avatars, portadas propias).
- Observabilidad: Falta Firebase Analytics y Performance Monitoring.

---

## 4. Objetivos
### 4.1. General
Desarrollar una app móvil social para gestión de lectura que permita organizar la biblioteca personal e interactuar con una comunidad de lectores.

### 4.2. Parciales
- Sistema de usuarios social: registro/ login, perfiles, seguidores, descubrimiento.
- Comunicación: chat en tiempo real, notificaciones, recomendaciones directas y comentarios.
- Valoraciones sociales: estrellas 1–5, reseñas, feed, “me gusta”.
- Biblioteca avanzada: estados, etiquetas y estadísticas.
- UX mejorada: rendimiento, búsqueda/filtros, modo oscuro y accesibilidad.

---

## 5. Arquitectura
### 5.1. Actual
- MVVM + Clean, Repositories.
- Room (SQLite) para persistencia local.
- Retrofit para lectura de datos (endpoint Firebase Realtime DB público).

### 5.2. Objetivo
- MVVM + Clean con DI (Dagger Hilt) y Navigation Component.
- Persistencia híbrida: Room (offline) + Firebase (Auth, Firestore, Storage, FCM).
- Sincronización offline/online con estrategias de cache (Flow/LiveData + listeners Firestore).

---

## 6. Modelo de Datos
### 6.1. Local (Room, ya implementado)
- `User(id, name, email, phone, password, signupDate)`
- `Book(id, title, coverImage)`
- `UserBook(userId, bookId, savedDate)` (relación muchos-a-muchos)

### 6.2. Remoto (Firestore, propuesto)
- `users/{userId}`: perfil, avatarUrl, bio, estadísticas, intereses.
- `follows/{userId}/following/{targetId}`: seguidores/seguidos.
- `books/{bookId}`: metadatos extendidos; colección `ratings/{uid}` y `reviews/{reviewId}`.
- `feeds/{userId}/items/{itemId}`: actividad social (valoraciones, reseñas, likes, seguimientos).
- `chats/{chatId}/messages/{messageId}`: mensajes (senderId, content, timestamp, readBy).
- `recommendations/{userId}/incoming/{recId}`: recomendaciones recibidas (estado aceptada/rechazada).

---

## 7. Diseño de Módulos Sociales
### 7.1. Autenticación (Firebase Auth)
- Email/Password y proveedores sociales (opcional).
- Enlace con documento `users/{uid}` en Firestore tras registro.

### 7.2. Perfiles y Seguimiento
- Pantalla de perfil social: avatar, bio, estadísticas y últimas actividades.
- Seguidores/Seguidos con consultas paginadas.

### 7.3. Chat en Tiempo Real
- Lista de conversaciones (último mensaje y no leídos).
- Vista de chat con envío/recepción en tiempo real (listeners Firestore) y FCM para push.

### 7.4. Valoraciones y Reseñas
- Estrellas 1–5, reseñas textuales, “me gusta” y feed de actividad.
- Moderación básica (edición/eliminación por autor, reportes mínimos).

### 7.5. Recomendaciones
- Basadas en intereses (tags/estados/valoraciones) + manuales entre usuarios.
- Aceptar/Rechazar con historial.

### 7.6. Biblioteca Avanzada
- Estados: leyendo, leído, abandonado, en pausa, por leer.
- Etiquetas personalizadas por usuario.
- Estadísticas (nº libros por estado, tiempo estimado, rachas).

---

## 8. Plan de Implantación (Fases y Actividades)
1) Refactor base (Hilt, Navigation) y setup Firebase (Auth, Firestore, Storage, FCM).  
2) Autenticación e identidad (perfil social mínimo en Firestore).  
3) Chat (conversaciones, mensajes, notificaciones).  
4) Recomendaciones (algoritmo básico + manual entre usuarios).  
5) Valoraciones y reseñas (con feed).  
6) Biblioteca avanzada (estados, etiquetas, estadísticas).  
7) Integración, optimización, pruebas e i18n (es_ES, en_US).  
8) Documentación y entrega.

Hitos (estimación): 2 + 3 + 3 + 2 + 2 + 2 + 3 + 1 semanas (ajustable).

---

## 9. Estrategia de Pruebas
- Unitarias: UseCases, Repositories (mocks), validaciones y utilidades.
- Integración: flujos Auth ↔ Firestore ↔ Room (sincronización y resoluciones de conflicto).
- UI (Espresso): login/registro, chat, guardar libros, valoraciones, navegación.
- Rendimiento: listeners Firestore, listas grandes, imágenes (Glide/Storage).
- Seguridad: reglas de Firestore, Auth obligatoria, scopes mínimos de FCM tokens.

---

## 10. Requisitos Técnicos
- Android 8.0+ (API 26+), Kotlin, Coroutines + Flow, LiveData o StateFlow.
- DI: Dagger Hilt; Navigation Component; ViewBinding/Compose (opcional).
- Firebase: Auth, Firestore, Storage, FCM, Analytics, Performance.
- Accesibilidad: tamaños texto escalables, contraste, contentDescription, navegación por teclado.

---

## 11. Seguridad y Privacidad
- Comunicación cifrada (HTTPS). Tokens gestionados por Firebase.
- Reglas de Firestore: acceso por propietario/seguidores según recurso; validaciones en servidor.
- Datos sensibles solo en Firestore; Room almacena datos mínimos para offline.
- Política de privacidad y consentimiento (si procede).

---

## 12. Internacionalización y UX
- Idiomas: `es_ES`, `en_US` (strings, fechas, plurales).
- Modo oscuro, tamaño de fuente configurable, ergonomía una mano.
- Búsqueda avanzada y filtros (estado, etiquetas, valoraciones, autor).

---

## 13. Control de Versiones y Entregables
- Repositorio: `https://github.com/smg-444/ProyectoLectoGuard`
- Rama principal: `main`
- Convención commit: tipo(scope): descripción (feat, fix, docs, refactor, test, chore).
- Releases por hito (tags). Issues y milestones por módulo/fase.

---

## 14. Próximos Pasos Inmediatos
1) Añadir dependencias Firebase (BOM) y Dagger Hilt.  
2) Configurar proyecto en Firebase Console y `google-services.json`.  
3) Implementar Auth (email/password) y pantalla de perfil enlazada con Firestore.  
4) Migrar navegación a Navigation Component.  
5) Preparar base del chat (modelo y colecciones).  

---

## 15. Anexos
- Build: Gradle AGP 8.10.1, Kotlin 2.0.21, Room 2.6.1, Retrofit 2.9.0.
- Estructura de carpetas y principales clases (ver `Doc.md`).
- Capturas de pantalla: se añadirán tras finalizar UI de módulos sociales.


