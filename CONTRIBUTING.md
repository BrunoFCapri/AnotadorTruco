# Cómo contribuir

Esta guía junta las convenciones que ya sigue el proyecto. Si es la primera vez que entrás al código, empezá por la [Guía del desarrollador](docs/GUIA_DESARROLLADOR.md).

## Flujo de trabajo

1. Partí de `main` y abrí una rama con un nombre corto que diga de qué se trata (`estilo-fileteado`, `reglas-flor`, `fix-deshacer`).
2. Hacé commits chicos, cada uno con un solo cambio que se entienda solo.
3. Antes de integrar, corré los tests y probá la app en un teléfono (ver [Antes de integrar](#antes-de-integrar)).
4. Integrá a `main` cuando la rama esté completa y probada.

## Commits

El historial está en castellano y cada mensaje sigue la misma forma:

```
Pintar una vez los fondos que no cambian

Qué pasaba y por qué hacía falta el cambio. Qué se hizo y qué se
descartó, si se probó otra cosa.

Cómo se comprobó que se ve igual (capturas, píxeles que cambian).

Mediciones antes y después, con el dispositivo y el build usados.
```

- **Título:** verbo en infinitivo (*Agregar*, *Mostrar*, *Ajustar*, *Pintar*), sin punto final y de 60 caracteres o menos. Dice qué cambia para quien usa o mantiene la app, no qué archivo se tocó.
- **Cuerpo:** explica el porqué. El qué ya está en el diff.
- **Cambios de rendimiento:** incluí la tabla con lo medido antes y después. Un cambio que dice ser más rápido sin números no se integra.
- **Cambios visuales que no deberían verse:** decí cómo comprobaste que la pantalla quedó igual (por ejemplo, capturas comparadas píxel a píxel).

## Estilo de código

- Estilo oficial de Kotlin (`kotlin.code.style=official` en `gradle.properties`), con comas finales en las listas de parámetros.
- **Comentarios en castellano**, para explicar el porqué y no repetir lo que dice el código. Si un número sale de un mockup o de una medición, el comentario lo dice.
- **Nombres:**
  - El modelo y las reglas usan inglés para lo técnico (`Game`, `Hand`, `Bet`, `Score`, `reduce`) y castellano para los términos del truco (`mano`, `EnvidoCall`, `Canto`, `malas`, `buenas`).
  - Las piezas visuales del fileteado van en castellano (`Filete`, `Cartel`, `Cinta`, `Voluta`, `Pintura`, `Tonos`, `casitas`), porque son nombres del oficio y no tienen traducción útil.
  - Seguí lo que ya usa el archivo que estás tocando.
- **Composables:** `modifier: Modifier = Modifier` como primer parámetro opcional, y los eventos como lambdas `onAlgo`.
- **Textos:** todo lo que se ve va en `res/values/strings.xml`, con `plurals` cuando lleva número. No escribas textos en el código.
- **Reglas del juego:** solo en `domain/`. La UI pregunta qué se puede hacer (`cantosFor`, `prompt`); nunca decide una regla por su cuenta.

## Tests

```bash
./gradlew testDebugUnitTest
```

- Toda regla nueva o cambiada del truco lleva su test en `GameEngineTest`. El nombre del test es una frase que describe la regla: `` `retruco no querido da dos al que lo canto y pasa la mano` ``.
- Si cambiás algo que se guarda (cualquier clase `@Serializable` dentro de `Session`), agregá un test que confirme que una partida guardada con la versión anterior se sigue leyendo. Ver la sección de compatibilidad en la guía.

## Antes de integrar

- [ ] `./gradlew testDebugUnitTest` pasa.
- [ ] `./gradlew assembleRelease` compila. R8 puede romper cosas que en debug andan.
- [ ] La app se probó en un teléfono, instalada con `./gradlew installRelease`.
- [ ] Una partida guardada con la versión anterior se sigue abriendo.
- [ ] Si tocaste la UI: probaste con la letra del sistema agrandada y en una pantalla angosta.
- [ ] Si el cambio es de rendimiento: mediste en release, en un teléfono de gama baja, y pusiste los números en el commit.

## Medir rendimiento

Las mediciones del historial se hicieron en un **Samsung Galaxy A04** (Android 14, gama baja), con el **build release** y el ahorro de energía activado. Es la referencia: si anda bien ahí, anda bien en casi cualquier teléfono.

- Siempre en release (`installRelease`). El debug corre interpretado y es varias veces más lento: no sirve para comparar.
- Arranque: mediana de 10 arranques en frío, leyendo `Displayed` y `Fully drawn` del logcat.
- Cuadros: `adb shell dumpsys gfxinfo com.apptruco framestats` después de un mismo recorrido de toques.
- Latencia y detalle por hilo: traza de Perfetto.

## Publicar una versión

El APK se publica en [GitHub Releases](https://github.com/BrunoFCapri/AnotadorTruco/releases), no en el historial de git. El README enlaza a `releases/latest/download/AnotadorTruco.apk`, que siempre baja la última versión. Por eso **el archivo tiene que llamarse siempre `AnotadorTruco.apk`**: la versión va en la etiqueta del release, no en el nombre.

1. En `app/build.gradle.kts`, subí `versionCode` (de a uno) y `versionName` (por ejemplo `1.1`).
2. Anotá los cambios en `CHANGELOG.md` y hacé el commit.
3. Generá el APK:
   ```bash
   ./gradlew assembleRelease
   mkdir -p dist && cp app/build/outputs/apk/release/app-release.apk dist/AnotadorTruco.apk
   ```
   La carpeta `dist/` está en `.gitignore`.
4. Hacé push y, en GitHub, entrá a **Releases → Draft a new release**:
   - **Tag:** `v` más la versión (`v1.1`), creada sobre `main`.
   - **Título:** `Truco 1.1`.
   - **Descripción:** lo de esa versión en el CHANGELOG.
   - **Archivos:** arrastrá `dist/AnotadorTruco.apk`.
   - Dejá marcado **Set as the latest release** y publicá.
5. Probá el enlace del README desde un teléfono.

### La clave de firma

El APK se firma con la clave de debug de la computadora donde se compila: `~/.android/debug.keystore`. Android solo instala una actualización encima si viene firmada con la **misma** clave. Si esa clave se pierde (reinstalar el sistema, cambiar de computadora), quien tenga la app instalada va a tener que desinstalarla, y perder su partida, para instalar la versión nueva.

- **Guardá una copia de `~/.android/debug.keystore`** en un lugar seguro. En otra computadora, copiala a esa misma ruta antes de compilar.
- Para publicar en Google Play hace falta una clave propia. Cambiar a otra clave obliga a todos a desinstalar una vez.

## Capturas del README

Las imágenes de `docs/capturas/` salen del emulador **Pixel 6** (1080×2400), con:

- la barra de estado en modo demo (9:30, batería llena, sin notificaciones);
- las animaciones del sistema apagadas, para que ninguna captura salga a mitad de una animación;
- una partida a 30, con flor y 4 jugadores, jugada desde la app hasta terminar 30 a 18.

Se guardan achicadas a 540×1200 en PNG a color completo. Con 256 colores pesan un tercio, pero los degradés salen con franjas.

Si cambia algo que se ve en ellas, conviene sacarlas de nuevo, todas en la misma situación.

## Referencias visuales

La carpeta `estilos/` tiene los mockups en HTML y los renders del estilo visual. Está en `.gitignore` y no se versiona: si la necesitás, pedísela al autor.
