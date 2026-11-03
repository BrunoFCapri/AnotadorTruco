# Historial de cambios

Los cambios importantes del proyecto, del más nuevo al más viejo. El formato sigue la idea de [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). Cada versión publicada tiene su APK en [Releases](https://github.com/BrunoFCapri/AnotadorTruco/releases).

## 1.0 — 2026-11-03

Primera versión publicada.

### Funciones

- Anotador de truco a 15 o a 30, con malas y buenas.
- 2, 4 o 6 jugadores, con pica-pica opcional.
- Con o sin flor.
- Asistente de cantos: envido, real envido, falta envido, flor, contraflor, contraflor al resto, truco, retruco y vale cuatro, con sus puntos querido y no querido.
- Casitas de fósforos animadas.
- Deshacer hasta 100 jugadas.
- Guardado automático de la partida.
- Pantalla de rendimiento con gráfico y puntos por origen.

### Estilo visual

- Estilo **Fileteado Porteño**: fondo negro con marco de filete, equipos como naipes crema con doble marco, pinta y volutas, cintas en cola de golondrina, casitas de fósforos pintados y letra fileteada dorada.
- Tipografías Playfair Display, Alegreya y Alegreya SC (licencia OFL).
- "Nueva partida" se ajusta a pantallas angostas y bajas sin tener que desplazarse.

### Rendimiento

Medido en un Galaxy A04, build release:

- La jugada se muestra en el acto y se guarda después: la latencia del toque bajó de 92/126 ms a 42/74 ms (p50/p90).
- Solo se recompone el panel del equipo que cambia.
- Las casitas se dibujan en su propia capa, con lo fijo cacheado.
- La letra fileteada se mide una sola vez en lugar de tres.
- Las fuentes se precargan en otro hilo mientras arranca la app: la partida se ve en 600 ms en lugar de 665 ms.
- Los fondos que no cambian se pintan una vez: la GPU tarda 8,9 ms por cuadro en lugar de 15,4 ms.
- Baseline profile con el código de la app, la serialización y DataStore.
