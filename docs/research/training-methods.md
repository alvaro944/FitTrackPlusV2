# Metodos de entrenamiento con base cientifica

**Fecha:** 2026-07-27
**Fase:** 0 del arco de periodizacion (`docs/design/mejoras-claude.md`, entrada 19)
**Estado:** para revision del dueño. Sin codigo asociado.

Este documento recoge los metodos ya estudiados que pueden sostener las funciones de periodizacion, progresion y objetivos de FitTrackPlus. Se modelan los PRINCIPIOS, que son hechos. No se reproduce texto de ninguna fuente.

---

## 0. Como leer este documento: niveles de evidencia

No todo lo que se usa en el gimnasio tiene el mismo respaldo. Mezclarlo todo al mismo nivel seria deshonesto y llevaria a programar la app con falsa confianza. Cada metodo va etiquetado:

| Nivel | Significado |
|---|---|
| **[A] Solido** | Meta-analisis o revisiones sistematicas convergentes. Se puede implementar con confianza. |
| **[B] Razonable** | Evidencia real pero limitada, mixta o con muestras pequeñas. Implementable como sugerencia, nunca como imposicion. |
| **[C] Heuristica de campo** | Practica extendida entre entrenadores, con logica fisiologica pero sin validacion fuerte. Util como valor por defecto configurable, jamas presentado al usuario como "la ciencia dice". |

Esta distincion es el resultado mas importante de esta fase. La app debe poder decir "esto es una recomendacion practica" y no disfrazar heuristicas de hechos.

---

## 1. Estimacion de 1RM (e1RM)

### Que dice la evidencia

Las ecuaciones de prediccion (Epley, Brzycki, Lombardi, Wathen) estiman el 1RM a partir de un peso y sus repeticiones. **[A]** El hallazgo consistente es que **el error crece con el numero de repeticiones**: las formulas coinciden entre si a repeticiones bajas y divergen cada vez mas segun subimos.

- Zona fiable: **1-6 repeticiones**. Ahi las formulas concuerdan dentro de un margen del 2-3%.
- Zona aceptable: hasta **~10 repeticiones**, con error creciente.
- Por encima de ~12: la estimacion es esencialmente ruido y no deberia usarse para decidir cargas.

En validaciones sobre sentadilla, Epley aplicada a series de 3 repeticiones no mostro diferencia significativa frente al 1RM real, pero **sobrestimo de forma significativa** al aplicarse a series de 5. Brzycki tiende a ser mas conservadora en el rango bajo.

### Que implica para la app

FitTrackPlus **ya calcula e1RM con Epley** (`ObserveWorkoutStatsUseCase.kt:240-246`), pero lo aplica a cualquier serie sin filtrar por repeticiones. Eso significa que una serie de 20 repeticiones esta generando un e1RM que se pinta en la grafica como si valiera lo mismo que una de 3. No vale lo mismo.

**Propuesta:**

1. Acotar el calculo de e1RM a series de **<= 10 repeticiones**. Por encima, no estimar (`null`), no mostrar el punto.
2. Marcar visualmente la **confianza** del punto: alta (1-6 reps), media (7-10). El usuario debe poder distinguir un e1RM fiable de uno inferido.
3. Mantener Epley como formula principal por continuidad con el historial ya registrado. Cambiar de formula reescribiria retroactivamente la curva del usuario, y eso es peor que el error de la formula.
4. No usar nunca el e1RM de una sola serie como verdad. Usar el mejor de un periodo, que es lo que ya hace `bestEstimatedOneRepMax`.

---

## 2. Zonas de intensidad (%1RM)

**[B]** La relacion entre %1RM y repeticiones posibles es real pero **muy variable entre personas y entre ejercicios**. Las tablas clasicas son orientativas, no deterministas: dos personas con el mismo 1RM pueden hacer numeros de repeticiones distintos al 80%.

La referencia clasica en fuerza es la tabla de Prilepin (Alexander Prilepin, halterofilia sovietica, 1960-70), construida sobre observacion de entrenamientos de alto nivel:

| %1RM | Reps por serie | Total optimo por sesion | Rango |
|---|---|---|---|
| 55-65 | 3-6 | 24 | 18-30 |
| 65-75 | 3-6 | 18 | 12-24 |
| 75-85 | 2-4 | 15 | 10-20 |
| 85-95 | 1-2 | 10 | 6-14 |
| 95-100 | 1 | 3 | 4-10 |

**Advertencia importante:** Prilepin se derivo de halteras (arrancada, dos tiempos) en atletas de elite. Extrapolarla a press banca en un usuario recreativo es un salto que la tabla no autoriza. **[C]** en ese contexto.

### Que implica para la app

Usar %1RM como **sugerencia de carga** es viable y util. Usarlo como prescripcion rigida no. La app deberia proponer un peso y dejar que el usuario lo corrija, aprendiendo de esa correccion.

---

## 3. Esquemas de progresion

### Progresion doble **[B]**

El mas adecuado para FitTrackPlus, y de largo. Funciona asi: se fija un rango (p.ej. 8-12). Se sube el peso solo cuando se alcanza el **techo del rango en todas las series**. Al subir peso, las repeticiones caen al suelo del rango y se vuelve a empezar.

Por que encaja: la app **ya tiene rangos de repeticiones** (`targetRepsText`) y **ya tiene una heuristica de progresion** (`GetProgressionHintUseCase`: media de reps de las ultimas 3 sesiones, si 2 o mas superan el techo → UP). Es decir, la progresion doble ya esta medio implementada sin llamarse asi.

**Propuesta:** formalizarla. La regla actual usa la *media* de repeticiones, que es mas permisiva que el criterio clasico (todas las series al techo). Es una decision defendible — media es mas estable frente a una serie mala — pero conviene documentarla como decision consciente, no como accidente.

### Progresion lineal **[B]**

Añadir peso fijo cada sesion. Funciona en principiantes y se agota rapido. Poco interesante para un usuario con historial.

### Autorregulacion por RIR/RPE **[A]**

RIR = repeticiones en reserva. El usuario reporta cuantas le quedaban. **[A]** Los meta-analisis muestran que la prescripcion autorregulada (por RIR/RPE o por velocidad) produce **mejoras de fuerza similares** a la prescripcion por porcentajes fijos. No es superior, pero tampoco inferior, y se adapta al dia a dia.

Sobre umbrales de perdida de velocidad **[A]**: para fuerza, perdidas <= 25% resultan mejores; para hipertrofia, > 25%. Esto requiere medir velocidad de barra — **fuera del alcance de la app**, no hay hardware.

**Propuesta:** RIR es un campo de entrada de una cifra por serie, opcional. Es el dato subjetivo con mejor relacion valor/coste de toda la app, y es la unica via para detectar fatiga sin sensores.

### Proximidad al fallo — "exprimir la serie" **[A]**

Este es el apartado que mas importa al dueño, asi que va con detalle.

Los meta-analisis comparando entrenamiento **al fallo** frente a **sin llegar al fallo** encuentran:

- **Sin diferencia significativa** en fuerza ni en hipertrofia entre ambas condiciones.
- Cuando el volumen **no** se iguala entre grupos, el analisis favorece significativamente al **no-fallo** para ganancias de fuerza. Traduccion: llegar al fallo cuesta volumen, y el volumen es lo que construye.
- En sujetos **entrenados**, hay un efecto favorable al fallo para hipertrofia, pero **el tamaño del efecto es pequeño**.
- **Entrenar cerca del fallo (1-3 RIR) produce hipertrofia similar a llegar al fallo completo.**

**Conclusion honesta y contraintuitiva:** el fallo no es necesario. Y llevar todas las series al fallo probablemente sea contraproducente, porque destruye el volumen que si podrias acumular. La zona util es **1-3 repeticiones en reserva**.

Esto tiene una consecuencia de producto muy concreta: lo valioso **no es empujar al usuario al fallo, sino ayudarle a vivir de forma consistente en la zona 1-3 RIR**. La app no debe premiar el fallo. Debe premiar la **consistencia en la zona efectiva** — y eso si es medible, y es exactamente el "control sobre las series y el esfuerzo" que se pide.

**Propuesta concreta:**
1. RIR opcional por serie (0 = fallo, 1, 2, 3, 4+).
2. Metrica de **calidad de esfuerzo**: qué porcentaje de tus series cae en la zona 1-3 RIR. Ni blandas (4+) ni al fallo constante (0).
3. Aviso cuando aparece un patron sostenido de RIR 0 en muchas series — no como regañina, sino como informacion: estas gastando fatiga sin comprar adaptacion extra.

---

## 4. Periodizacion

**[A]** Los meta-analisis comparando periodizacion lineal (LP) frente a ondulante diaria (DUP) encuentran:

- **Hipertrofia**: sin diferencia significativa entre modelos.
- **Fuerza**: ambos funcionan, con una **ventaja modesta para el modelo ondulante**, que se amplia cuando se iguala el trabajo total.

Traduccion honesta: **el modelo de periodizacion importa menos de lo que la industria vende**. Lo que importa es acumular volumen suficiente de forma progresiva y sostenible. La periodizacion organiza, no es magia.

Modelos:

- **Lineal**: empieza con volumen alto e intensidad baja, progresa hacia intensidad alta y volumen bajo.
- **Ondulante (DUP/WUP)**: alterna intensidad y volumen dentro de la misma semana o entre semanas.
- **Por bloques**: fases con enfasis distinto (acumulacion → intensificacion → realizacion).

### Que implica para la app

La arquitectura de bloques que ya diseñamos (mesociclo = lista ordenada de bloques; bloque = rutina + prescripcion + duracion) **soporta los tres modelos sin cambios**. Un modelo lineal es una secuencia de bloques con rangos descendentes; uno ondulante alterna prescripciones. Esto valida el diseño: no hay que elegir un modelo de periodizacion en el codigo, solo permitir componer bloques.

---

## 5. Descargas (deloads) — el apartado que exige mas rigor

Esto es lo que pediste con mas insistencia, asi que aqui va sin adornos, incluida la parte incomoda.

### 5.1 Que hace la gente realmente

Una encuesta transversal sobre practicas de descarga en deportes de fuerza y fisico (n=245, mayoritariamente powerlifters) da numeros concretos:

**Frecuencia y duracion:**
- Entre descargas: **5,6 ± 2,3 semanas** (rango 1-12)
- Duracion: **6,4 ± 1,7 dias** (rango 1-14)
- Halterofilos descargan mas a menudo (4,8 ± 1,1 semanas); strongman menos (6,7 ± 3,4)

**Que reducen:**
- 78,9% reduce **series semanales**
- 52,8% reduce repeticiones por serie
- 83,7% reduce **carga en multiarticulares** (60,2% en monoarticulares)
- 84,9% reduce **proximidad al fallo** en multiarticulares
- 63,0% **mantiene la frecuencia** de sesiones
- 70,3% mantiene el numero de ejercicios; 89,0% mantiene el rango de movimiento

El patron es claro: **se recorta volumen y esfuerzo, no se deja de ir al gimnasio**. Una descarga no es una semana libre.

**Por que descargan:** reducir fatiga (92,3%), preparar cambio de bloque (64,6%), mejorar rendimiento (59,8%), competicion (51,2%).

**Cuando descargan:**
- 65,4% por **calendario programado**
- 62,6% cuando aparecen **agujetas persistentes o molestias articulares**
- 54,1% cuando el **rendimiento se estanca**

### 5.2 La parte incomoda: la evidencia experimental es mixta

**[B], tirando a debil.** Un ensayo con una descarga de 1 semana en el punto medio de un programa de 9 semanas encontro que la descarga **perjudico** la fuerza de tren inferior, sin efecto sobre hipertrofia, potencia ni resistencia muscular.

Es decir: la descarga esta universalmente extendida entre practicantes, pero **no esta demostrado que mejore las adaptaciones**. Su justificacion solida es la gestion de fatiga y la prevencion del sobreentrenamiento no funcional, no "crecer mas".

**Consecuencia de diseño no negociable: la app NUNCA debe imponer una descarga.** Debe sugerirla, explicar por que, y permitir ignorarla sin friccion. Presentar como obligatorio algo que un ensayo controlado señala como potencialmente contraproducente para la fuerza seria mentir al usuario.

### 5.3 Volumen: los famosos MEV / MAV / MRV

Marcadores de volumen popularizados por Renaissance Periodization (Israetel):

- **MEV** (volumen minimo efectivo): ~6-8 series por grupo muscular y semana
- **MAV** (volumen maximo adaptativo): ~12-20 series/semana
- **MRV** (volumen maximo recuperable): ~10-20 series/semana segun unas fuentes, 20-30+ segun otras

**Etiqueta honesta: [C].** Y hay que decirlo claro — la dispersion entre fuentes (10-20 frente a 20-30+ para el mismo concepto) delata que **estos numeros no estan validados**, son marcos practicos. Lo que si esta respaldado **[A]** es la relacion dosis-respuesta: el meta-analisis de Schoenfeld/Krieger/Ogborn encontro que **cada serie semanal adicional se asocia a ~0,37% mas de ganancia de tamaño muscular**, con relacion graduada. Mas volumen, mas crecimiento — dentro de lo recuperable.

Lo que NO existe es un umbral validado de "a partir de N series estas sobreentrenado". El MRV es un concepto util para pensar, no una constante que se pueda codificar.

### 5.4 Propuesta concreta para la app

Traduciendo todo lo anterior a lo que FitTrackPlus **puede medir de verdad** con los datos que ya registra:

| Disparador | Base | ¿Puede medirlo la app hoy? |
|---|---|---|
| **Tiempo desde la ultima descarga** | 5,6 ± 2,3 semanas | **Si.** Sesiones finalizadas y fechas ya estan en `workout_sessions`. |
| **Estancamiento de rendimiento** | 54,1% lo usa como criterio | **Si.** El e1RM ya se calcula. Detectar N semanas sin mejora del mejor e1RM es directo. |
| **Acumulacion de volumen por musculo** | Dosis-respuesta [A] | **NO.** Ver problema abajo. |
| **Molestias articulares / agujetas** | 62,6% lo usa | **No sin input del usuario.** Requiere entrada subjetiva. |

**Problema encontrado, y es importante:** pediste que la descarga se dispare por "tanto volumen en un tiempo". Ese es el criterio correcto conceptualmente, pero **el volumen relevante es por grupo muscular**, y FitTrackPlus **no sabe hoy que musculo trabaja cada ejercicio**. Solo tiene el nombre del ejercicio como texto.

Esto conecta directamente con la **entrada 18 del backlog** (dataset de ejercicios), que trae `primaryMuscles` y `secondaryMuscles` para 1.324 ejercicios. **El disparador por volumen depende de esa integracion.** Las dos entradas del backlog no eran independientes: la 18 es prerrequisito del criterio de descarga que quieres.

Mientras tanto, se puede calcular volumen **por ejercicio** (series x reps x peso, ya disponible en `sessionVolumes`) y detectar picos relativos a la linea base del propio usuario. Es un sustituto pobre pero honesto.

**Regla propuesta (sugerencia, nunca imposicion):**

Sugerir descarga cuando se cumplan **al menos dos** de:
1. Han pasado >= 5 semanas desde la ultima descarga o desde el inicio del bloque.
2. El mejor e1RM del ejercicio principal no mejora en >= 3 semanas.
3. El volumen semanal esta por encima de la mediana personal de las ultimas 8 semanas de forma sostenida.
4. El usuario ha reportado molestias o RIR sistematicamente bajo.

Exigir dos criterios evita el falso positivo de una sola semana mala.

**Protocolo propuesto** (siguiendo lo que hace la gente, no lo que suena bien):
- Duracion: **1 semana**
- **Mantener** frecuencia de sesiones, ejercicios y rango de movimiento
- **Reducir series a la mitad** (es el ajuste mas usado: 78,9%)
- **Reducir carga ~10-20%** en multiarticulares
- **Alejarse del fallo**: dejar 3-4 repeticiones en reserva

Esto es exactamente lo que describiste tu ("mismo peso, mitad de series") con el matiz de bajar tambien algo de carga en los basicos.

---

## 6. Proyeccion a objetivo

El caso planteado: "quiero 100 kg en banca, hoy hago 80x5".

### Ritmo realista **[C]**

Los datos disponibles son de divulgacion, no de meta-analisis, y varian mucho. El consenso practico para un usuario intermedio ronda **2,5 kg cada 2-4 semanas** en el 1RM, es decir del orden de **1-4 kg/mes**, decreciendo con la experiencia. Un principiante progresa mucho mas rapido; un avanzado puede tardar un año en esos mismos kilos.

**Etiqueta [C] y hay que ser muy claro con esto**: cualquier fecha estimada que muestre la app es una proyeccion de baja confianza. Debe presentarse como orientacion ("a este ritmo, ~5 meses") y nunca como promesa. Y debe recalcularse con los datos reales del usuario, que valen mas que cualquier media poblacional.

### El margen de seguridad que planteaste

Tu intuicion era correcta y tiene fundamento: apuntar a un e1RM de ~110 para asegurar 100 reales. La razon esta en el apartado 1 — **las formulas sobrestiman**, y Epley significativamente a partir de 5 repeticiones. Un e1RM de 100 calculado desde series de 8 no significa que puedas con 100.

**Propuesta:** el objetivo se define como peso real levantable. La app calcula el e1RM objetivo aplicando un margen (~5-10%) y, cuando el e1RM se acerca, **sugiere una serie de validacion a 3 repeticiones** — que es la zona donde la formula es fiable. Ahi se confirma o se ajusta.

Esto convierte tu intuicion en un procedimiento: no se declara el objetivo cumplido por formula, se **verifica** en la zona de repeticiones donde la formula no miente.

---

## 7. Minimo viable (decidido 2026-07-27)

El dueño acoto el alcance tras leer la investigacion: **la descarga deja de ser prioridad**. Su volumen de entrenamiento actual es moderado, recupera bien y sigue progresando, asi que el problema que la descarga resuelve no es su problema hoy. Lo que si quiere es **control sobre la calidad del esfuerzo**: mejora por ejercicio, series, y proximidad al fallo.

Es la decision correcta, y la investigacion la respalda: la evidencia de la descarga es mixta **[B]**, mientras que la de proximidad al fallo es **[A]** y ademas es accionable con los datos que ya se registran.

**Entra:**

1. **Acotar e1RM a <= 10 reps + indicador de confianza.** Corrige un defecto real del calculo actual. Cimiento de todo lo demas.
2. **Campo RIR opcional, una cifra por ejercicio** (no por serie). Es la medida directa de "cuanto exprimo el ejercicio" y no existe hoy. Decision del dueño: **por ejercicio al terminarlo**, no por serie, para minimizar la friccion con el movil en la mano en el gimnasio. Se pierde la caida entre series, pero un campo que no se rellena no vale nada.
3. **Metrica de calidad de esfuerzo**: porcentaje de ejercicios en la zona 1-3 RIR. Traduce la evidencia [A] en una señal util.
4. **Progresion doble formalizada.** Ya existe medio implementada en `GetProgressionHintUseCase`; documentarla y hacerla explicita.
5. **Objetivo por ejercicio** con margen de seguridad y serie de validacion a 3 reps.

**No entra (aparcado, no descartado):**

- **Descargas.** Se conserva toda la investigacion del apartado 5 para cuando el volumen de entrenamiento suba. Si algun dia entra, entra como sugerencia por tiempo + estancamiento, nunca impuesta.
- **Descarga por volumen muscular.** Depende de la entrada 18 (dataset). Sin prisa ahora.
- **Bloques y periodizacion completa.** El diseño ya esta validado y soporta los tres modelos; se implementa cuando haya datos reales de RIR y e1RM acotado.

**Nota de coherencia:** el orden no cambia respecto al plan aprobado. La Fase 1 (reps estructuradas) sigue siendo el primer paso, porque los puntos 3 y 4 de esta lista necesitan rangos estructurados para funcionar.

---

## Fuentes

- [Deloading Practices in Strength and Physique Sports: A Cross-sectional Survey](https://pmc.ncbi.nlm.nih.gov/articles/PMC10948666/)
- [Gaining more from doing less? The effects of a one-week deload period during supervised resistance training on muscular adaptations (PeerJ)](https://peerj.com/articles/16777/)
- [A Practical Approach to Deloading: Recommendations and Considerations for Strength and Physique Sports](https://shura.shu.ac.uk/35313/3/Bell-APracticalApproach(AM).pdf)
- [Effects of deload periods in resistance training on muscle hypertrophy and strength endurance (Scientific Reports)](https://www.nature.com/articles/s41598-026-40612-5)
- [Dose-response relationship between weekly resistance training volume and increases in muscle mass (Schoenfeld, Ogborn, Krieger)](https://www.tandfonline.com/doi/full/10.1080/02640414.2016.1210197)
- [The Resistance Training Dose Response: Meta-Regressions on Weekly Volume and Frequency](https://pubmed.ncbi.nlm.nih.gov/41343037/)
- [The Effect of Load and Volume Autoregulation on Muscular Strength and Hypertrophy: A Systematic Review and Meta-Analysis](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8762534/)
- [Autoregulated resistance training for maximal strength enhancement: systematic review and network meta-analysis](https://www.sciencedirect.com/science/article/pii/S1728869X25000590)
- [Effects of linear and daily undulating periodized resistance training programs on measures of muscle hypertrophy: systematic review and meta-analysis](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5571788/)
- [Validation of the Brzycki and Epley Equations for the 1RM Back Squat Test](https://opensiuc.lib.siu.edu/cgi/viewcontent.cgi?article=1744&context=gs_rp)
- [Training Volume Landmarks for Muscle Growth (Renaissance Periodization)](https://rpstrength.com/blogs/articles/training-volume-landmarks-muscle-growth) — marco practico, no validado
- [Prilepin's Chart (referencia clasica de halterofilia)](https://powerliftingtechnique.com/prilepins-chart/)
- [Effects of resistance training performed to repetition failure or non-failure on muscular strength and hypertrophy: systematic review and meta-analysis](https://www.sciencedirect.com/science/article/pii/S2095254621000077)
- [Influence of Resistance Training Proximity-to-Failure on Skeletal Muscle Hypertrophy: Systematic Review with Meta-analysis](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9935748/)
