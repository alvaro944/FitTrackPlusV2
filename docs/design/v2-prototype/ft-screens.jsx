// FitTrackPlus — All Screens
// Depends on ft-components.jsx being loaded first

// ── Sample Data ───────────────────────────────────────────────
const FT_ROUTINES = [
  {
    id: 'r1', name: 'Empuje · Fuerza', days: ['Lun', 'Jue'], active: true,
    lastUsed: 'Hace 1 día',
    exercises: [
      { id: 'e1', name: 'Press de banca plano', sets: 4, reps: '6–8' },
      { id: 'e2', name: 'Press inclinado mancuernas', sets: 3, reps: '10–12' },
      { id: 'e3', name: 'Fondos en paralelas', sets: 3, reps: 'Al fallo' },
      { id: 'e4', name: 'Press militar con barra', sets: 4, reps: '6–8' },
      { id: 'e5', name: 'Elevaciones laterales', sets: 3, reps: '15' },
    ],
  },
  {
    id: 'r2', name: 'Tirón · Fuerza', days: ['Mar', 'Vie'], active: false,
    lastUsed: 'Hace 3 días',
    exercises: [
      { id: 'e6', name: 'Peso muerto convencional', sets: 4, reps: '5' },
      { id: 'e7', name: 'Remo con barra', sets: 4, reps: '6–8' },
      { id: 'e8', name: 'Dominadas', sets: 3, reps: 'Al fallo' },
      { id: 'e9', name: 'Curl con barra', sets: 3, reps: '10' },
    ],
  },
  {
    id: 'r3', name: 'Pierna · Fuerza', days: ['Mié'], active: false,
    lastUsed: 'Hace 5 días',
    exercises: [
      { id: 'e10', name: 'Sentadilla trasera', sets: 4, reps: '5–6' },
      { id: 'e11', name: 'Prensa de pierna', sets: 3, reps: '10–12' },
      { id: 'e12', name: 'Peso muerto rumano', sets: 3, reps: '10' },
      { id: 'e13', name: 'Extensión cuádriceps', sets: 3, reps: '15' },
    ],
  },
];

const FT_SESSIONS = [
  {
    id: 's1', routineId: 'r1', name: 'Empuje · Fuerza',
    dateLabel: 'Mié 22 abr', dateGroup: 'Esta semana',
    duration: 62, volume: 8420, setsCount: 17,
    exercises: [
      { name: 'Press de banca plano', sets: [{w:90,r:6,pr:false},{w:90,r:6,pr:false},{w:87.5,r:7,pr:false},{w:85,r:8,pr:false}] },
      { name: 'Press inclinado mancuernas', sets: [{w:28,r:12,pr:false},{w:28,r:11,pr:false},{w:26,r:12,pr:false}] },
      { name: 'Fondos en paralelas', sets: [{w:0,r:12,pr:false},{w:0,r:10,pr:false},{w:0,r:9,pr:false}] },
      { name: 'Press militar con barra', sets: [{w:60,r:8,pr:true},{w:60,r:7,pr:false},{w:57.5,r:8,pr:false},{w:57.5,r:7,pr:false}] },
      { name: 'Elevaciones laterales', sets: [{w:12,r:15,pr:false},{w:12,r:15,pr:false},{w:10,r:15,pr:false}] },
    ],
  },
  {
    id: 's2', routineId: 'r2', name: 'Tirón · Fuerza',
    dateLabel: 'Mar 21 abr', dateGroup: 'Esta semana',
    duration: 55, volume: 7340, setsCount: 14,
    exercises: [
      { name: 'Peso muerto convencional', sets: [{w:130,r:5,pr:false},{w:130,r:5,pr:false},{w:125,r:5,pr:false},{w:125,r:5,pr:false}] },
      { name: 'Remo con barra', sets: [{w:80,r:8,pr:false},{w:80,r:7,pr:false},{w:75,r:8,pr:false},{w:75,r:8,pr:false}] },
      { name: 'Dominadas', sets: [{w:0,r:9,pr:true},{w:0,r:8,pr:false},{w:0,r:7,pr:false}] },
      { name: 'Curl con barra', sets: [{w:42.5,r:10,pr:false},{w:42.5,r:9,pr:false},{w:40,r:10,pr:false}] },
    ],
  },
  {
    id: 's3', routineId: 'r3', name: 'Pierna · Fuerza',
    dateLabel: 'Lun 20 abr', dateGroup: 'Esta semana',
    duration: 68, volume: 12800, setsCount: 14,
    exercises: [
      { name: 'Sentadilla trasera', sets: [{w:110,r:5,pr:false},{w:110,r:5,pr:false},{w:107.5,r:6,pr:false},{w:105,r:6,pr:false}] },
      { name: 'Prensa de pierna', sets: [{w:180,r:12,pr:false},{w:180,r:11,pr:false},{w:160,r:12,pr:false}] },
      { name: 'Peso muerto rumano', sets: [{w:90,r:10,pr:true},{w:90,r:10,pr:false},{w:85,r:10,pr:false}] },
      { name: 'Extensión cuádriceps', sets: [{w:60,r:15,pr:false},{w:60,r:15,pr:false},{w:55,r:15,pr:false},{w:55,r:15,pr:false}] },
    ],
  },
  {
    id: 's4', routineId: 'r1', name: 'Empuje · Fuerza',
    dateLabel: 'Jue 17 abr', dateGroup: 'Semana pasada',
    duration: 58, volume: 7980, setsCount: 17,
    exercises: [
      { name: 'Press de banca plano', sets: [{w:87.5,r:6,pr:false},{w:87.5,r:6,pr:false},{w:85,r:7,pr:false},{w:82.5,r:8,pr:false}] },
      { name: 'Press inclinado mancuernas', sets: [{w:26,r:12,pr:false},{w:26,r:11,pr:false},{w:24,r:12,pr:false}] },
    ],
  },
  {
    id: 's5', routineId: 'r2', name: 'Tirón · Fuerza',
    dateLabel: 'Mar 15 abr', dateGroup: 'Semana pasada',
    duration: 53, volume: 7100, setsCount: 14,
    exercises: [],
  },
];

const FT_STATS = {
  thisWeek: { sessions: 3, volume: 28560, streak: 5 },
  weeklyVolume: [
    { label: 'L', value: 12800, highlight: false },
    { label: 'M', value: 7340, highlight: false },
    { label: 'X', value: 8420, highlight: true },
    { label: 'J', value: 0, highlight: false },
    { label: 'V', value: 0, highlight: false },
    { label: 'S', value: 0, highlight: false },
    { label: 'D', value: 0, highlight: false },
  ],
  records: [
    { exercise: 'Press de banca plano', value: '90', unit: 'kg', date: '22 abr' },
    { exercise: 'Sentadilla trasera', value: '110', unit: 'kg', date: '20 abr' },
    { exercise: 'Peso muerto convencional', value: '130', unit: 'kg', date: '21 abr' },
    { exercise: 'Dominadas', value: '9', unit: 'reps', date: '21 abr' },
    { exercise: 'Press militar', value: '60', unit: 'kg', date: '22 abr' },
  ],
  exerciseProgress: [
    { name: 'Press de banca plano', history: [82.5, 85, 87.5, 90], unit: 'kg' },
    { name: 'Sentadilla trasera', history: [100, 105, 107.5, 110], unit: 'kg' },
    { name: 'Peso muerto', history: [120, 125, 127.5, 130], unit: 'kg' },
  ],
};

// ── Inicio Screen ─────────────────────────────────────────────
function InicioScreen({ onNavigate }) {
  const activeRoutine = FT_ROUTINES.find(r => r.active);
  const lastSession = FT_SESSIONS[0];
  const today = new Date();
  const hour = today.getHours();
  const greeting = hour < 13 ? 'Buenos días' : hour < 20 ? 'Buenas tardes' : 'Buenas noches';
  const weekDays = ['D','L','M','X','J','V','S'];
  const todayIdx = today.getDay();

  const sessionDays = [1, 2, 3]; // Mon, Tue, Wed have sessions this week (indices in week)

  return (
    <div style={{ padding: '8px 20px 20px', display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Greeting */}
      <div>
        <div style={{ fontSize: 13, color: FT.textTertiary, fontFamily: FT_FONT.sans, marginBottom: 3 }}>
          Miércoles, 22 de abril
        </div>
        <div style={{ fontSize: 26, fontWeight: 700, color: FT.textPrimary, fontFamily: FT_FONT.sans, letterSpacing: '-0.02em', lineHeight: 1.15 }}>
          {greeting} 👋
        </div>
      </div>

      {/* Week strip */}
      <div style={{
        display: 'flex', gap: 4,
        background: FT.surface, borderRadius: 14, padding: '12px 10px',
        border: `1px solid ${FT.borderLight}`,
      }}>
        {weekDays.map((d, i) => {
          const isToday = i === todayIdx;
          const hasSession = sessionDays.includes(i);
          return (
            <div key={i} style={{
              flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
            }}>
              <span style={{ fontSize: 11, color: FT.textTertiary, fontFamily: FT_FONT.sans, fontWeight: 500 }}>{d}</span>
              <div style={{
                width: 30, height: 30, borderRadius: 10,
                background: isToday ? FT.primary : 'transparent',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                flexDirection: 'column', gap: 2,
              }}>
                <span style={{ fontSize: 13, fontWeight: isToday ? 700 : 500, color: isToday ? '#fff' : FT.textPrimary, fontFamily: FT_FONT.sans }}>
                  {16 + i}
                </span>
              </div>
              <div style={{
                width: 5, height: 5, borderRadius: 999,
                background: hasSession ? FT.primary : FT.borderLight,
                opacity: hasSession ? 0.8 : 1,
              }}/>
            </div>
          );
        })}
      </div>

      {/* Next workout card */}
      {activeRoutine && (
        <FTCard
          onClick={() => onNavigate('entrenar')}
          style={{ background: FT.primaryDark, border: 'none', padding: '20px' }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 14 }}>
            <div>
              <FTBadge label="Próximo entrenamiento" variant="active" />
              <div style={{ fontSize: 20, fontWeight: 700, color: '#fff', marginTop: 8, fontFamily: FT_FONT.sans, letterSpacing: '-0.01em' }}>
                {activeRoutine.name}
              </div>
              <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.55)', marginTop: 3, fontFamily: FT_FONT.sans }}>
                Hoy · {activeRoutine.exercises.length} ejercicios
              </div>
            </div>
            <div style={{
              width: 42, height: 42, borderRadius: 14,
              background: 'rgba(255,255,255,0.12)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <Ico.Play size={20} color="#fff" />
            </div>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {activeRoutine.exercises.slice(0,3).map(ex => (
              <span key={ex.id} style={{
                fontSize: 12, color: 'rgba(255,255,255,0.6)',
                background: 'rgba(255,255,255,0.1)',
                borderRadius: 8, padding: '4px 10px',
                fontFamily: FT_FONT.sans,
              }}>{ex.name}</span>
            ))}
            {activeRoutine.exercises.length > 3 && (
              <span style={{
                fontSize: 12, color: 'rgba(255,255,255,0.45)',
                fontFamily: FT_FONT.sans, padding: '4px 2px',
              }}>+{activeRoutine.exercises.length - 3} más</span>
            )}
          </div>
        </FTCard>
      )}

      {/* Quick stats */}
      <div style={{ display: 'flex', gap: 12 }}>
        <FTCard style={{ flex: 1, padding: '16px' }}>
          <FTMetric value="3" label="Sesiones · semana" accent="primary" />
        </FTCard>
        <FTCard style={{ flex: 1, padding: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 2 }}>
            <Ico.Flame size={14} color={FT.accentWarm} />
          </div>
          <FTMetric value="5" label="Días racha" accent="warm" />
        </FTCard>
      </div>

      {/* Last session */}
      <div>
        <FTSection label="Última sesión" right="Ver todo" />
        <FTCard onClick={() => onNavigate('detalleHistorial', { session: lastSession })}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
            <div>
              <div style={{ fontSize: 15, fontWeight: 600, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>{lastSession.name}</div>
              <div style={{ fontSize: 12.5, color: FT.textSecondary, fontFamily: FT_FONT.sans, marginTop: 2 }}>{lastSession.dateLabel}</div>
            </div>
            <Ico.ChevronRight size={18} color={FT.textTertiary}/>
          </div>
          <div style={{ display: 'flex', gap: 20 }}>
            <FTMetric value={lastSession.duration} unit="min" label="Duración" />
            <FTMetric value={(lastSession.volume/1000).toFixed(1)} unit="t" label="Volumen" />
            <FTMetric value={lastSession.setsCount} label="Series" />
          </div>
        </FTCard>
      </div>
    </div>
  );
}

// ── Rutinas Screen ────────────────────────────────────────────
function RutinasScreen({ routines, setRoutines, onNavigate }) {
  const [menuOpen, setMenuOpen] = React.useState(null);

  const activeRoutine = routines.find(r => r.active);

  const handleActivate = (id) => {
    setRoutines(prev => prev.map(r => ({ ...r, active: r.id === id })));
    setMenuOpen(null);
  };

  const handleArchive = (id) => {
    setRoutines(prev => prev.filter(r => r.id !== id));
    setMenuOpen(null);
  };

  return (
    <div style={{ padding: '8px 20px 80px', position: 'relative' }}>
      {/* Active indicator */}
      {activeRoutine && (
        <div style={{
          marginBottom: 18, padding: '10px 14px',
          background: FT.primarySoft, borderRadius: 12,
          border: `1px solid ${FT.primary}20`,
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <Ico.Check size={16} color={FT.primary} />
          <span style={{ fontSize: 13, color: FT.primary, fontFamily: FT_FONT.sans, fontWeight: 600 }}>
            Rutina activa: {activeRoutine.name}
          </span>
        </div>
      )}

      <FTSection label={`${routines.length} rutinas`} />

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {routines.map(routine => (
          <div key={routine.id} style={{ position: 'relative' }}>
            <FTCard
              onClick={() => onNavigate('editorRutina', { routine })}
              style={{ padding: '16px' }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                    <span style={{ fontSize: 16, fontWeight: 700, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>{routine.name}</span>
                    {routine.active && <FTBadge label="Activa" variant="active" />}
                  </div>
                  <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 10 }}>
                    <span style={{ fontSize: 12.5, color: FT.textSecondary, fontFamily: FT_FONT.sans }}>
                      {routine.exercises.length} ejercicios
                    </span>
                    <span style={{ fontSize: 12.5, color: FT.textSecondary, fontFamily: FT_FONT.sans }}>
                      {routine.days.join(' · ')}
                    </span>
                    <span style={{ fontSize: 12.5, color: FT.textTertiary, fontFamily: FT_FONT.sans }}>
                      {routine.lastUsed}
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5 }}>
                    {routine.exercises.slice(0,3).map(ex => (
                      <span key={ex.id} style={{
                        fontSize: 11.5, color: FT.textSecondary,
                        background: FT.surfaceAlt, borderRadius: 6, padding: '3px 8px',
                        fontFamily: FT_FONT.sans,
                      }}>{ex.name}</span>
                    ))}
                    {routine.exercises.length > 3 && (
                      <span style={{ fontSize: 11.5, color: FT.textTertiary, fontFamily: FT_FONT.sans, padding: '3px 4px' }}>
                        +{routine.exercises.length - 3}
                      </span>
                    )}
                  </div>
                </div>
                <button
                  onClick={e => { e.stopPropagation(); setMenuOpen(menuOpen === routine.id ? null : routine.id); }}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '4px', borderRadius: 8, outline: 'none', marginLeft: 8, flexShrink: 0 }}
                >
                  <Ico.MoreVert />
                </button>
              </div>
            </FTCard>

            {/* Context menu */}
            {menuOpen === routine.id && (
              <div style={{
                position: 'absolute', top: 48, right: 12, zIndex: 20,
                background: FT.surface, borderRadius: 14, padding: '6px',
                border: `1px solid ${FT.border}`,
                boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
                minWidth: 160,
              }}>
                <button onClick={() => { onNavigate('editorRutina', { routine }); setMenuOpen(null); }} style={{ width:'100%', display:'flex', alignItems:'center', gap:10, padding:'10px 12px', background:'none', border:'none', cursor:'pointer', borderRadius:10, outline:'none' }}>
                  <Ico.Edit size={16} color={FT.textSecondary} />
                  <span style={{ fontSize:14, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>Editar</span>
                </button>
                {!routine.active && (
                  <button onClick={() => handleActivate(routine.id)} style={{ width:'100%', display:'flex', alignItems:'center', gap:10, padding:'10px 12px', background:'none', border:'none', cursor:'pointer', borderRadius:10, outline:'none' }}>
                    <Ico.Check size={16} color={FT.primary} />
                    <span style={{ fontSize:14, color: FT.primary, fontFamily: FT_FONT.sans, fontWeight:600 }}>Activar</span>
                  </button>
                )}
                <div style={{ height:1, background: FT.borderLight, margin:'4px 8px' }}/>
                <button onClick={() => handleArchive(routine.id)} style={{ width:'100%', display:'flex', alignItems:'center', gap:10, padding:'10px 12px', background:'none', border:'none', cursor:'pointer', borderRadius:10, outline:'none' }}>
                  <Ico.Archive size={16} color={FT.error} />
                  <span style={{ fontSize:14, color: FT.error, fontFamily: FT_FONT.sans }}>Archivar</span>
                </button>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* FAB */}
      <FTFAB onClick={() => onNavigate('editorRutina', { routine: null })} />

      {/* Dismiss menu overlay */}
      {menuOpen && (
        <div onClick={() => setMenuOpen(null)} style={{ position:'fixed', inset:0, zIndex:10 }}/>
      )}
    </div>
  );
}

// ── Entrenar Screen ───────────────────────────────────────────
function EntrenarScreen({ routines, onNavigate }) {
  const active = routines.find(r => r.active);

  if (!active) {
    return (
      <div style={{ padding: '8px 20px' }}>
        <FTEmpty
          icon={<Ico.Dumbbell size={28} color={FT.primary} />}
          title="Sin rutina activa"
          message="Activa una rutina en la sección Rutinas para empezar a entrenar."
          ctaLabel="Ir a rutinas"
          onCta={() => onNavigate('rutinas')}
        />
      </div>
    );
  }

  return (
    <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Day context */}
      <div style={{ fontSize: 13, color: FT.textTertiary, fontFamily: FT_FONT.sans }}>
        Semana 14 · Sesión 3 de 3
      </div>

      {/* Main workout card */}
      <div style={{
        background: FT.primaryDark, borderRadius: 20, padding: '24px',
        overflow: 'hidden', position: 'relative',
      }}>
        <div style={{
          position: 'absolute', top: -20, right: -20,
          width: 120, height: 120, borderRadius: '50%',
          background: 'rgba(255,255,255,0.04)',
        }}/>
        <div style={{
          fontSize: 12, color: 'rgba(255,255,255,0.5)', fontFamily: FT_FONT.sans,
          textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 8,
        }}>Entrenamiento de hoy</div>
        <div style={{
          fontSize: 24, fontWeight: 700, color: '#fff',
          fontFamily: FT_FONT.sans, letterSpacing: '-0.02em', marginBottom: 4,
        }}>{active.name}</div>
        <div style={{
          fontSize: 13.5, color: 'rgba(255,255,255,0.5)', marginBottom: 20,
          fontFamily: FT_FONT.sans,
        }}>
          {active.exercises.length} ejercicios · ~65 min estimados
        </div>

        {/* Exercise list preview */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 24 }}>
          {active.exercises.map((ex, i) => (
            <div key={ex.id} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              padding: '10px 14px', borderRadius: 12,
              background: 'rgba(255,255,255,0.07)',
            }}>
              <span style={{
                width: 22, height: 22, borderRadius: 7,
                background: 'rgba(255,255,255,0.1)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 11, fontWeight: 700, color: 'rgba(255,255,255,0.5)',
                fontFamily: FT_FONT.mono, flexShrink: 0,
              }}>{i+1}</span>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, fontWeight: 600, color: 'rgba(255,255,255,0.9)', fontFamily: FT_FONT.sans }}>{ex.name}</div>
                <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.4)', fontFamily: FT_FONT.sans }}>{ex.sets} series · {ex.reps} reps</div>
              </div>
            </div>
          ))}
        </div>

        {/* CTA */}
        <button
          onClick={() => onNavigate('sesionActiva', { routine: active })}
          style={{
            width: '100%', padding: '16px', borderRadius: 14,
            background: '#fff', border: 'none',
            fontSize: 16, fontWeight: 700, color: FT.primaryDark,
            fontFamily: FT_FONT.sans, cursor: 'pointer', letterSpacing: '-0.01em',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
            outline: 'none',
          }}
        >
          <Ico.Play size={18} color={FT.primaryDark} />
          Comenzar entrenamiento
        </button>
      </div>

      {/* Recent sessions */}
      <div>
        <FTSection label="Sesiones recientes" />
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {FT_SESSIONS.slice(0,3).map(s => (
            <FTCard key={s.id} onClick={() => onNavigate('detalleHistorial', { session: s })} style={{ padding: '14px 16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontSize: 14, fontWeight: 600, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>{s.name}</div>
                  <div style={{ fontSize: 12.5, color: FT.textTertiary, fontFamily: FT_FONT.sans, marginTop: 2 }}>{s.dateLabel} · {s.duration} min</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span style={{ fontSize: 14, fontWeight: 600, color: FT.textSecondary, fontFamily: FT_FONT.mono }}>
                    {(s.volume/1000).toFixed(1)}t
                  </span>
                  <Ico.ChevronRight />
                </div>
              </div>
            </FTCard>
          ))}
        </div>
      </div>
    </div>
  );
}

// ── Historial Screen ──────────────────────────────────────────
function HistorialScreen({ onNavigate }) {
  const groups = {};
  FT_SESSIONS.forEach(s => {
    if (!groups[s.dateGroup]) groups[s.dateGroup] = [];
    groups[s.dateGroup].push(s);
  });

  return (
    <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Summary chips */}
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <FTChip label="Todas" active={true} />
        <FTChip label="Empuje" active={false} />
        <FTChip label="Tirón" active={false} />
        <FTChip label="Pierna" active={false} />
      </div>

      {Object.entries(groups).map(([group, sessions]) => (
        <div key={group}>
          <FTSection label={group} />
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {sessions.map(s => (
              <FTCard key={s.id} onClick={() => onNavigate('detalleHistorial', { session: s })} style={{ padding: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                  <div>
                    <div style={{ fontSize: 15, fontWeight: 600, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>{s.name}</div>
                    <div style={{ fontSize: 12.5, color: FT.textSecondary, fontFamily: FT_FONT.sans, marginTop: 2 }}>{s.dateLabel}</div>
                  </div>
                  <Ico.ChevronRight />
                </div>
                <div style={{ display: 'flex', gap: 20 }}>
                  <FTMetric value={s.duration} unit="min" label="Duración" />
                  <FTMetric value={(s.volume/1000).toFixed(1)} unit="t" label="Volumen" />
                  <FTMetric value={s.setsCount || '—'} label="Series" />
                </div>
              </FTCard>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

// ── Datos Screen ──────────────────────────────────────────────
function DatosScreen() {
  const [period, setPeriod] = React.useState('semana');
  return (
    <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Period selector */}
      <div style={{ display: 'flex', gap: 8 }}>
        {['semana','mes','total'].map(p => (
          <FTChip key={p} label={p.charAt(0).toUpperCase()+p.slice(1)} active={period===p} onClick={() => setPeriod(p)} />
        ))}
      </div>

      {/* Key stats */}
      <div style={{ display: 'flex', gap: 10 }}>
        <FTCard style={{ flex: 1, padding: '16px' }}>
          <FTMetric value="3" label="Sesiones" size="lg" accent="primary" />
        </FTCard>
        <FTCard style={{ flex: 1, padding: '16px' }}>
          <FTMetric value="28.6" unit="t" label="Volumen" size="lg" />
        </FTCard>
        <FTCard style={{ flex: 1, padding: '16px' }}>
          <FTMetric value="5" label="Racha" size="lg" accent="warm" />
        </FTCard>
      </div>

      {/* Volume chart */}
      <FTCard style={{ padding: '18px 16px' }}>
        <FTSection label="Volumen por sesión (kg)" />
        <FTBarChart data={FT_STATS.weeklyVolume} height={80} />
      </FTCard>

      {/* Best records */}
      <div>
        <FTSection label="Mejores marcas" />
        <FTCard style={{ padding: '4px 16px' }}>
          {FT_STATS.records.map((rec, i) => (
            <div key={i}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '13px 0' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <div style={{ width: 32, height: 32, borderRadius: 10, background: FT.accentSoft, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Ico.Trophy size={15} color={FT.accentWarm} />
                  </div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 500, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>{rec.exercise}</div>
                    <div style={{ fontSize: 11.5, color: FT.textTertiary, fontFamily: FT_FONT.sans }}>Mejor · {rec.date}</div>
                  </div>
                </div>
                <span style={{ fontSize: 16, fontWeight: 700, color: FT.accentWarm, fontFamily: FT_FONT.mono }}>
                  {rec.value}<span style={{ fontSize: 11, marginLeft: 2, color: FT.textTertiary }}>{rec.unit}</span>
                </span>
              </div>
              {i < FT_STATS.records.length - 1 && <FTDivider />}
            </div>
          ))}
        </FTCard>
      </div>

      {/* Progress by exercise */}
      <div>
        <FTSection label="Progreso por ejercicio" />
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {FT_STATS.exerciseProgress.map((ex, i) => {
            const first = ex.history[0];
            const last = ex.history[ex.history.length - 1];
            const gain = last - first;
            const pct = Math.round((gain / first) * 100);
            return (
              <FTCard key={i} style={{ padding: '14px 16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                  <span style={{ fontSize: 14, fontWeight: 600, color: FT.textPrimary, fontFamily: FT_FONT.sans }}>{ex.name}</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Ico.ArrowUp size={14} color={FT.primary} />
                    <span style={{ fontSize: 13, fontWeight: 700, color: FT.primary, fontFamily: FT_FONT.mono }}>+{gain}{ex.unit}</span>
                    <span style={{ fontSize: 12, color: FT.textTertiary, fontFamily: FT_FONT.sans }}> · {pct}%</span>
                  </div>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                  {ex.history.map((v, j) => (
                    <div key={j} style={{ textAlign: 'center' }}>
                      <div style={{
                        fontSize: 13, fontWeight: j === ex.history.length-1 ? 700 : 400,
                        color: j === ex.history.length-1 ? FT.textPrimary : FT.textTertiary,
                        fontFamily: FT_FONT.mono,
                      }}>{v}</div>
                      <div style={{ fontSize: 10, color: FT.textTertiary, fontFamily: FT_FONT.sans }}>S{j+1}</div>
                    </div>
                  ))}
                </div>
                <FTProgressBar value={last} max={last * 1.2} color={FT.primary} height={4} />
              </FTCard>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ── Editor de Rutina Screen ───────────────────────────────────
function EditorRutinaScreen({ routine: initialRoutine, onBack, onSave }) {
  const isNew = !initialRoutine;
  const [name, setName] = React.useState(initialRoutine?.name || '');
  const [days, setDays] = React.useState(initialRoutine?.days || []);
  const [exercises, setExercises] = React.useState(initialRoutine?.exercises || []);
  const [saved, setSaved] = React.useState(false);

  const allDays = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];

  const toggleDay = (d) => setDays(prev => prev.includes(d) ? prev.filter(x=>x!==d) : [...prev,d]);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => { onBack(); }, 700);
  };

  const removeExercise = (id) => setExercises(prev => prev.filter(e => e.id !== id));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <FTHeader
        title={isNew ? 'Nueva rutina' : 'Editar rutina'}
        onBack={onBack}
        right={
          <button onClick={handleSave} style={{
            background: saved ? FT.primarySoft : FT.primary, border: 'none', borderRadius: 10,
            padding: '8px 16px', color: saved ? FT.primary : '#fff',
            fontSize: 14, fontWeight: 600, fontFamily: FT_FONT.sans, cursor: 'pointer', outline: 'none',
            transition: 'all 0.2s',
          }}>
            {saved ? '✓ Guardado' : 'Guardar'}
          </button>
        }
      />
      <div style={{ flex: 1, overflowY: 'auto', padding: '4px 20px 40px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 22 }}>
          <FTTextField label="Nombre de la rutina" value={name} onChange={setName} placeholder="Ej. Empuje · Fuerza" />

          {/* Days */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: FT.textSecondary, fontFamily: FT_FONT.sans, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>Días de entrenamiento</div>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
              {allDays.map(d => (
                <button key={d} onClick={() => toggleDay(d)} style={{
                  padding: '8px 14px', borderRadius: 10,
                  background: days.includes(d) ? FT.primary : FT.surfaceAlt,
                  color: days.includes(d) ? '#fff' : FT.textSecondary,
                  border: 'none', fontSize: 13.5, fontWeight: days.includes(d) ? 700 : 400,
                  fontFamily: FT_FONT.sans, cursor: 'pointer', outline: 'none',
                  transition: 'all 0.15s',
                }}>{d}</button>
              ))}
            </div>
          </div>

          {/* Exercises */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <div style={{ fontSize: 12, fontWeight: 600, color: FT.textSecondary, fontFamily: FT_FONT.sans, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Ejercicios ({exercises.length})</div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {exercises.map((ex, i) => (
                <FTCard key={ex.id} style={{ padding: '12px 14px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontSize: 14.5, fontWeight: 600, color: FT.textPrimary, fontFamily: FT_FONT.sans, marginBottom: 4 }}>{ex.name}</div>
                      <div style={{ display: 'flex', gap: 10 }}>
                        <span style={{
                          fontSize: 12, color: FT.textSecondary, background: FT.surfaceAlt,
                          borderRadius: 6, padding: '3px 10px', fontFamily: FT_FONT.sans,
                        }}>{ex.sets} series</span>
                        <span style={{
                          fontSize: 12, color: FT.textSecondary, background: FT.surfaceAlt,
                          borderRadius: 6, padding: '3px 10px', fontFamily: FT_FONT.sans,
                        }}>{ex.reps} reps</span>
                      </div>
                    </div>
                    <button onClick={() => removeExercise(ex.id)} style={{
                      background: FT.errorSoft, border: 'none', borderRadius: 8,
                      width: 30, height: 30, display: 'flex', alignItems: 'center', justifyContent: 'center',
                      cursor: 'pointer', outline: 'none', marginLeft: 8,
                    }}>
                      <Ico.Close size={14} color={FT.error} />
                    </button>
                  </div>
                </FTCard>
              ))}
              <button onClick={() => {}} style={{
                width: '100%', padding: '14px', borderRadius: 14,
                border: `1.5px dashed ${FT.border}`, background: 'transparent',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                fontSize: 14, fontWeight: 600, color: FT.primary, fontFamily: FT_FONT.sans,
                cursor: 'pointer', outline: 'none',
              }}>
                <Ico.Plus size={16} color={FT.primary} />
                Añadir ejercicio
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Sesión Activa Screen ──────────────────────────────────────
function SesionActivaScreen({ routine, onBack }) {
  const [currentEx, setCurrentEx] = React.useState(0);
  const [currentSet, setCurrentSet] = React.useState(0);
  const [sets, setSets] = React.useState(
    routine.exercises.map(ex => Array.from({length: ex.sets}, () => ({ weight:'', reps:'', done:false })))
  );
  const [elapsed, setElapsed] = React.useState(0);
  const [restTimer, setRestTimer] = React.useState(null);
  const [finished, setFinished] = React.useState(false);

  React.useEffect(() => {
    const t = setInterval(() => setElapsed(p => p+1), 1000);
    return () => clearInterval(t);
  }, []);

  React.useEffect(() => {
    if (restTimer === null) return;
    if (restTimer <= 0) { setRestTimer(null); return; }
    const t = setTimeout(() => setRestTimer(p => p-1), 1000);
    return () => clearTimeout(t);
  }, [restTimer]);

  const formatTime = s => `${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`;

  const updateSet = (exIdx, setIdx, field, val) => {
    setSets(prev => {
      const next = prev.map(e => [...e]);
      next[exIdx][setIdx] = { ...next[exIdx][setIdx], [field]: val };
      return next;
    });
  };

  const completeSet = (exIdx, setIdx) => {
    updateSet(exIdx, setIdx, 'done', true);
    setRestTimer(90);
    const exSets = sets[exIdx];
    if (setIdx < exSets.length - 1) setCurrentSet(setIdx+1);
    else if (exIdx < routine.exercises.length - 1) {
      setCurrentEx(exIdx+1);
      setCurrentSet(0);
    }
  };

  if (finished) {
    return (
      <div style={{ display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', height:'100%', padding:'32px', gap:16, textAlign:'center' }}>
        <div style={{ width:72, height:72, borderRadius:24, background:FT.primarySoft, display:'flex', alignItems:'center', justifyContent:'center', marginBottom:8 }}>
          <Ico.Check size={32} color={FT.primary} />
        </div>
        <div style={{ fontSize:24, fontWeight:700, color:FT.textPrimary, fontFamily:FT_FONT.sans }}>¡Sesión completada!</div>
        <div style={{ fontSize:14, color:FT.textSecondary, fontFamily:FT_FONT.sans }}>{routine.name} · {formatTime(elapsed)}</div>
        <div style={{ display:'flex', gap:16, marginTop:8 }}>
          <FTMetric value={formatTime(elapsed)} label="Duración" />
          <FTMetric value={routine.exercises.length} label="Ejercicios" />
        </div>
        <div style={{marginTop:16}}>
          <FTButton label="Volver al inicio" onClick={onBack} />
        </div>
      </div>
    );
  }

  const ex = routine.exercises[currentEx];
  const totalSets = routine.exercises.reduce((a,e) => a + e.sets, 0);
  const doneSets = sets.flat().filter(s=>s.done).length;

  return (
    <div style={{ display:'flex', flexDirection:'column', height:'100%' }}>
      {/* Header */}
      <div style={{ padding:'12px 20px 8px', background:FT.bg, display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <button onClick={onBack} style={{ background:'none', border:'none', cursor:'pointer', outline:'none' }}>
          <Ico.Close size={22} color={FT.textSecondary} />
        </button>
        <div style={{ textAlign:'center' }}>
          <div style={{ fontSize:20, fontWeight:700, color:FT.textPrimary, fontFamily:FT_FONT.mono, letterSpacing:'-0.02em' }}>
            {formatTime(elapsed)}
          </div>
          <div style={{ fontSize:11.5, color:FT.textTertiary, fontFamily:FT_FONT.sans }}>
            {doneSets}/{totalSets} series
          </div>
        </div>
        <button
          onClick={() => setFinished(true)}
          style={{ background: FT.primarySoft, border:'none', borderRadius:10, padding:'8px 14px', color:FT.primary, fontSize:13, fontWeight:700, fontFamily:FT_FONT.sans, cursor:'pointer', outline:'none' }}
        >
          Finalizar
        </button>
      </div>

      {/* Progress bar */}
      <div style={{ padding:'0 20px 8px' }}>
        <FTProgressBar value={doneSets} max={totalSets} color={FT.primary} height={4} />
      </div>

      {/* Rest timer overlay */}
      {restTimer !== null && (
        <div style={{ margin:'0 20px', padding:'10px 16px', background:FT.primarySoft, borderRadius:12, display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
          <div style={{ display:'flex', alignItems:'center', gap:8 }}>
            <Ico.Timer size={16} color={FT.primary} />
            <span style={{ fontSize:13.5, color:FT.primary, fontFamily:FT_FONT.sans, fontWeight:600 }}>Descanso</span>
          </div>
          <div style={{ display:'flex', alignItems:'center', gap:10 }}>
            <span style={{ fontSize:20, fontWeight:700, color:FT.primary, fontFamily:FT_FONT.mono }}>{formatTime(restTimer)}</span>
            <button onClick={() => setRestTimer(null)} style={{ background:'none', border:'none', cursor:'pointer', outline:'none' }}>
              <Ico.Close size={16} color={FT.primary} />
            </button>
          </div>
        </div>
      )}

      <div style={{ flex:1, overflowY:'auto', padding:'8px 20px 24px' }}>
        {/* Exercise nav */}
        <div style={{ display:'flex', gap:6, marginBottom:16, overflowX:'auto', paddingBottom:4 }}>
          {routine.exercises.map((e,i) => {
            const exDone = sets[i].every(s=>s.done);
            const exActive = i === currentEx;
            return (
              <button key={e.id} onClick={() => {setCurrentEx(i); setCurrentSet(0);}} style={{
                flexShrink:0, padding:'6px 12px', borderRadius:10,
                background: exDone ? FT.primarySoft : exActive ? FT.primary : FT.surfaceAlt,
                color: exDone ? FT.primary : exActive ? '#fff' : FT.textSecondary,
                border:'none', fontSize:12.5, fontWeight:600, fontFamily:FT_FONT.sans, cursor:'pointer', outline:'none',
                transition:'all 0.15s',
              }}>
                {exDone ? '✓ ' : ''}{i+1}. {e.name.split(' ').slice(0,2).join(' ')}
              </button>
            );
          })}
        </div>

        {/* Current exercise */}
        <div style={{ marginBottom:16 }}>
          <div style={{ fontSize:11.5, color:FT.textTertiary, fontFamily:FT_FONT.sans, textTransform:'uppercase', letterSpacing:'0.06em', marginBottom:4 }}>
            Ejercicio {currentEx+1} de {routine.exercises.length}
          </div>
          <div style={{ fontSize:22, fontWeight:700, color:FT.textPrimary, fontFamily:FT_FONT.sans, letterSpacing:'-0.02em', lineHeight:1.2 }}>
            {ex.name}
          </div>
          <div style={{ fontSize:13.5, color:FT.textSecondary, fontFamily:FT_FONT.sans, marginTop:4 }}>
            {ex.sets} series · {ex.reps} reps
          </div>
        </div>

        {/* Sets table */}
        <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
          {/* Header */}
          <div style={{ display:'grid', gridTemplateColumns:'32px 1fr 1fr 80px', gap:8, padding:'0 4px', marginBottom:2 }}>
            {['#','Peso (kg)','Reps',''].map((h,i) => (
              <div key={i} style={{ fontSize:11, color:FT.textTertiary, fontFamily:FT_FONT.sans, fontWeight:600, textTransform:'uppercase', letterSpacing:'0.06em' }}>{h}</div>
            ))}
          </div>
          {sets[currentEx].map((s, si) => (
            <div key={si} style={{
              display:'grid', gridTemplateColumns:'32px 1fr 1fr 80px', gap:8, alignItems:'center',
              padding:'8px 4px', borderRadius:10,
              background: s.done ? FT.primarySoft : si === currentSet ? FT.surfaceAlt : 'transparent',
              transition:'background 0.15s',
            }}>
              <span style={{ fontSize:13, fontWeight:700, color: s.done ? FT.primary : FT.textTertiary, fontFamily:FT_FONT.mono }}>{si+1}</span>
              <input
                type="number" value={s.weight} onChange={e => updateSet(currentEx,si,'weight',e.target.value)}
                disabled={s.done}
                placeholder="—"
                style={{
                  width:'100%', padding:'10px 8px', borderRadius:10, boxSizing:'border-box',
                  border:`1.5px solid ${s.done ? FT.primarySoft : FT.border}`,
                  background: s.done ? FT.primarySoft : FT.surface,
                  fontSize:15, fontWeight:700, fontFamily:FT_FONT.mono, color: s.done ? FT.primary : FT.textPrimary,
                  outline:'none', textAlign:'center',
                }}
              />
              <input
                type="number" value={s.reps} onChange={e => updateSet(currentEx,si,'reps',e.target.value)}
                disabled={s.done}
                placeholder="—"
                style={{
                  width:'100%', padding:'10px 8px', borderRadius:10, boxSizing:'border-box',
                  border:`1.5px solid ${s.done ? FT.primarySoft : FT.border}`,
                  background: s.done ? FT.primarySoft : FT.surface,
                  fontSize:15, fontWeight:700, fontFamily:FT_FONT.mono, color: s.done ? FT.primary : FT.textPrimary,
                  outline:'none', textAlign:'center',
                }}
              />
              {s.done ? (
                <div style={{ display:'flex', alignItems:'center', justifyContent:'center' }}>
                  <div style={{ width:28, height:28, borderRadius:999, background:FT.primary, display:'flex', alignItems:'center', justifyContent:'center' }}>
                    <Ico.Check size={14} color="#fff" />
                  </div>
                </div>
              ) : (
                <FTButton label="Hecho" size="sm" onClick={() => completeSet(currentEx, si)} variant={si===currentSet?'primary':'ghost'} />
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ── Detalle Historial Screen ──────────────────────────────────
function DetalleHistorialScreen({ session, onBack }) {
  return (
    <div style={{ display:'flex', flexDirection:'column', height:'100%' }}>
      <FTHeader title={session.name} subtitle={session.dateLabel} onBack={onBack} />
      <div style={{ flex:1, overflowY:'auto', padding:'4px 20px 32px' }}>
        {/* Stats summary */}
        <FTCard style={{ marginBottom:20, padding:'18px 20px' }}>
          <div style={{ display:'flex', gap:24 }}>
            <FTMetric value={session.duration} unit="min" label="Duración" />
            <FTMetric value={(session.volume/1000).toFixed(1)} unit="t" label="Volumen" />
            <FTMetric value={session.setsCount || session.exercises.reduce((a,e)=>a+e.sets.length,0)} label="Series" />
          </div>
        </FTCard>

        <FTSection label="Ejercicios" />

        <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
          {session.exercises.map((ex, i) => (
            <FTCard key={i} style={{ padding:'14px 16px' }}>
              <div style={{ fontSize:15, fontWeight:700, color:FT.textPrimary, fontFamily:FT_FONT.sans, marginBottom:12 }}>
                {ex.name}
              </div>
              {/* Sets header */}
              <div style={{ display:'grid', gridTemplateColumns:'28px 1fr 1fr 1fr', gap:6, marginBottom:6 }}>
                {['#','Peso','Reps','Vol.'].map((h,j) => (
                  <div key={j} style={{ fontSize:10.5, color:FT.textTertiary, fontFamily:FT_FONT.sans, fontWeight:600, textTransform:'uppercase', letterSpacing:'0.05em' }}>{h}</div>
                ))}
              </div>
              <div style={{ display:'flex', flexDirection:'column', gap:4 }}>
                {ex.sets.map((set, si) => {
                  const vol = set.w > 0 ? set.w * set.r : set.r;
                  return (
                    <div key={si} style={{
                      display:'grid', gridTemplateColumns:'28px 1fr 1fr 1fr', gap:6, alignItems:'center',
                      padding:'6px 4px', borderRadius:6,
                      background: set.pr ? FT.accentSoft : si%2===0 ? FT.bg : 'transparent',
                    }}>
                      <span style={{ fontSize:12, color:FT.textTertiary, fontFamily:FT_FONT.mono }}>{si+1}</span>
                      <div style={{ display:'flex', alignItems:'center', gap:4 }}>
                        <span style={{ fontSize:13.5, fontWeight:600, color:FT.textPrimary, fontFamily:FT_FONT.mono }}>
                          {set.w > 0 ? `${set.w}kg` : 'PC'}
                        </span>
                      </div>
                      <span style={{ fontSize:13.5, fontWeight:600, color:FT.textPrimary, fontFamily:FT_FONT.mono }}>{set.r}</span>
                      <div style={{ display:'flex', alignItems:'center', gap:4 }}>
                        <span style={{ fontSize:13, color:FT.textSecondary, fontFamily:FT_FONT.mono }}>{vol}</span>
                        {set.pr && <FTBadge label="PR" variant="warm" />}
                      </div>
                    </div>
                  );
                })}
              </div>
            </FTCard>
          ))}
        </div>
      </div>
    </div>
  );
}

// Export
Object.assign(window, {
  FT_ROUTINES, FT_SESSIONS, FT_STATS,
  InicioScreen, RutinasScreen, EntrenarScreen,
  HistorialScreen, DatosScreen,
  EditorRutinaScreen, SesionActivaScreen, DetalleHistorialScreen,
});
