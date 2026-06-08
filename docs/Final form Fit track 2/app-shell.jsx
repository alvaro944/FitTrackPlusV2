// FitTrackPlus — App Shell + All Screens
const { useState, useEffect, useRef, useCallback, useMemo } = React;
const M = window.FTMockData;

// ─── ICONS (inline SVG) ──────────────────────────────────────────────────────
const Icon = {
  Home: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9.5L12 3l9 6.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z"/><path d="M9 21V12h6v9"/></svg>,
  Dumbbell: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M6.5 6.5h11m-11 11h11M6.5 6.5v11M17.5 6.5v11M3 9.5h4v5H3zM17 9.5h4v5h-4z"/></svg>,
  Play: () => <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5.14v14l11-7-11-7z"/></svg>,
  History: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>,
  BarChart: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="14" width="4" height="7"/><rect x="10" y="9" width="4" height="12"/><rect x="17" y="4" width="4" height="17"/></svg>,
  Settings: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></svg>,
  Plus: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>,
  ChevronRight: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 18 15 12 9 6"/></svg>,
  ChevronLeft: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="15 18 9 12 15 6"/></svg>,
  ChevronDown: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"/></svg>,
  Check: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>,
  X: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>,
  Archive: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>,
  Edit: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>,
  Duplicate: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>,
  Trash: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>,
  Timer: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="13" r="8"/><polyline points="12 9 12 13 14 15"/><line x1="9" y1="1" x2="15" y2="1"/></svg>,
  TrendingUp: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>,
  TrendingDown: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/></svg>,
  Minus: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><line x1="5" y1="12" x2="19" y2="12"/></svg>,
  Sun: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>,
  Moon: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/></svg>,
  Monitor: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>,
  Weight: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="7" r="4"/><path d="M6 21v-2a4 4 0 014-4h4a4 4 0 014 4v2"/></svg>,
  Fire: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2c0 0-5 4-5 9a5 5 0 0010 0c0-5-5-9-5-9z"/><path d="M12 12c0 0-2 1.5-2 3a2 2 0 004 0c0-1.5-2-3-2-3z"/></svg>,
  Calendar: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>,
  ArrowUp: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>,
  ArrowDown: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>,
  Dot: () => <svg viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="4"/></svg>,
  Pause: () => <svg viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>,
  RotateCcw: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>,
  Zap: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>,
  Info: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>,
  Menu: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>,
  Smartphone: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>,
  Download: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>,
  Widget: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>,
  Move: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="5 9 2 12 5 15"/><polyline points="9 5 12 2 15 5"/><polyline points="15 19 12 22 9 19"/><polyline points="19 9 22 12 19 15"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="12" y1="2" x2="12" y2="22"/></svg>,
};

// ─── HELPERS ────────────────────────────────────────────────────────────────
function formatDate(iso) {
  const d = new Date(iso);
  return d.toLocaleDateString('es-ES', { weekday:'short', day:'numeric', month:'short' });
}
function formatDateShort(iso) {
  const d = new Date(iso);
  return d.toLocaleDateString('es-ES', { day:'numeric', month:'short' });
}
function formatTime(secs) {
  const m = Math.floor(secs / 60).toString().padStart(2,'0');
  const s = (secs % 60).toString().padStart(2,'0');
  return `${m}:${s}`;
}
function formatVolume(v) {
  if (v >= 1000) return (v/1000).toFixed(1) + 'k';
  return Math.round(v).toString();
}
function getWeekNumber(d) {
  const date = new Date(d);
  date.setHours(0,0,0,0);
  date.setDate(date.getDate() + 4 - (date.getDay()||7));
  const yearStart = new Date(date.getFullYear(),0,1);
  return Math.ceil((((date - yearStart) / 86400000) + 1)/7);
}
function estimate1RM(weight, reps) {
  return Math.round(weight * (1 + reps / 30));
}

// ─── TOAST ───────────────────────────────────────────────────────────────────
function Toast({ message, visible }) {
  return (
    <div className="toast" style={{ opacity: visible ? 1 : 0 }}>{message}</div>
  );
}

// ─── BOTTOM NAV ──────────────────────────────────────────────────────────────
const NAV_TABS = [
  { id: 'home',     label: 'Inicio',   icon: 'Home' },
  { id: 'routines', label: 'Rutinas',  icon: 'Dumbbell' },
  { id: 'workout',  label: 'Entrenar', icon: 'Play' },
  { id: 'history',  label: 'Historial',icon: 'History' },
  { id: 'stats',    label: 'Datos',    icon: 'BarChart' },
];

// ─── SIDE DRAWER (hamburger menu) ────────────────────────────────────────────
function SideDrawer({ open, onClose, onNavigate, theme, onThemeChange, weightUnit, onUnitChange, showToast }) {
  if (!open) return null;
  const themeOptions = [
    { id: 'light', label: 'Claro', icon: 'Sun' },
    { id: 'dark', label: 'Oscuro', icon: 'Moon' },
    { id: 'system', label: 'Sistema', icon: 'Smartphone' },
  ];
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 95 }}>
      {/* Scrim */}
      <div onClick={onClose} style={{
        position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)',
        animation: 'fadeIn 0.2s ease',
      }} />
      {/* Panel slides from right */}
      <DrawerPanel>
        {/* Header */}
        <div style={{ padding: '20px 20px 16px', borderBottom: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: 1.2, color: 'var(--text3)', textTransform: 'uppercase', marginBottom: 2 }}>Menú</div>
            <div style={{ fontWeight: 700, fontSize: 17, letterSpacing: '-0.3px' }}>FitTrack+</div>
          </div>
          <button className="btn btn-ghost btn-icon" onClick={onClose} style={{ color: 'var(--text2)' }}>
            <div style={{ width: 20, height: 20 }}><Icon.X /></div>
          </button>
        </div>

        {/* Theme picker */}
        <div style={{ padding: '16px 20px 8px' }}>
          <div className="t-label t-secondary" style={{ marginBottom: 10 }}>Tema</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 6 }}>
            {themeOptions.map(opt => {
              const IconComp = Icon[opt.icon] || Icon.Settings;
              const isActive = theme === opt.id;
              return (
                <button key={opt.id} onClick={() => onThemeChange(opt.id)} style={{
                  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
                  padding: '12px 8px', borderRadius: 10,
                  background: isActive ? 'var(--primary)' : 'var(--surface-alt)',
                  color: isActive ? '#fff' : 'var(--text2)',
                  border: 'none', cursor: 'pointer', transition: 'var(--transition)',
                }}>
                  <div style={{ width: 18, height: 18 }}><IconComp /></div>
                  <span style={{ fontSize: 11, fontWeight: 600 }}>{opt.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Weight unit */}
        <div style={{ padding: '16px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ fontWeight: 500, fontSize: 14 }}>Unidad</div>
            <div className="t-tiny t-secondary">Peso por defecto</div>
          </div>
          <div className="segment-control" style={{ width: 'auto' }}>
            {['kg', 'lb'].map(u => (
              <button key={u} className={`segment ${weightUnit === u ? 'active' : ''}`}
                style={{ padding: '6px 16px', fontFamily: 'var(--font-mono)' }}
                onClick={() => onUnitChange(u)}>{u}</button>
            ))}
          </div>
        </div>

        <div style={{ height: 1, background: 'var(--border)', margin: '0 20px' }} />

        {/* Menu items */}
        <div style={{ padding: '8px 0' }}>
          {[
            { id: 'settings', label: 'Ajustes avanzados', icon: 'Settings', desc: 'Notificaciones, datos, cuenta' },
            { id: 'widget',   label: 'Widget & atajos',   icon: 'Smartphone', desc: 'Pantalla de inicio, accesos' },
          ].map(item => {
            const IconComp = Icon[item.icon] || Icon.Settings;
            return (
              <button key={item.id} onClick={() => { onNavigate(item.id); onClose(); }} style={{
                width: '100%', display: 'flex', alignItems: 'center', gap: 14,
                padding: '12px 20px', background: 'transparent', border: 'none',
                cursor: 'pointer', textAlign: 'left', transition: 'background 0.15s',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--surface-alt)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
              >
                <div style={{ width: 20, height: 20, color: 'var(--text2)' }}><IconComp /></div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 500, fontSize: 14 }}>{item.label}</div>
                  <div style={{ fontSize: 11, color: 'var(--text3)' }}>{item.desc}</div>
                </div>
                <div style={{ width: 16, height: 16, color: 'var(--text3)' }}><Icon.ChevronRight /></div>
              </button>
            );
          })}
          <button onClick={() => { showToast('Datos exportados (mock)'); onClose(); }} style={{
            width: '100%', display: 'flex', alignItems: 'center', gap: 14,
            padding: '12px 20px', background: 'transparent', border: 'none',
            cursor: 'pointer', textAlign: 'left',
          }}>
            <div style={{ width: 20, height: 20, color: 'var(--text2)' }}><Icon.Download /></div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 500, fontSize: 14 }}>Exportar datos</div>
              <div style={{ fontSize: 11, color: 'var(--text3)' }}>CSV con todas tus sesiones</div>
            </div>
          </button>
        </div>

        {/* Footer */}
        <div style={{ marginTop: 'auto', padding: '20px', borderTop: '1px solid var(--border)' }}>
          <div style={{ fontSize: 12, color: 'var(--text3)', fontFamily: 'var(--font-mono)' }}>FitTrack+ · v1.4.0</div>
          <div style={{ fontSize: 11, color: 'var(--text3)', marginTop: 2 }}>Build 2026.05 · Mineral</div>
        </div>
      </DrawerPanel>
    </div>
  );
}

function DrawerPanel({ children }) {
  const [shown, setShown] = useState(false);
  useEffect(() => {
    const id = requestAnimationFrame(() => setShown(true));
    return () => cancelAnimationFrame(id);
  }, []);
  return (
    <div style={{
      position: 'absolute', top: 0, right: 0, bottom: 0,
      width: '82%', maxWidth: 320,
      background: 'var(--surface)',
      boxShadow: '-8px 0 32px rgba(0,0,0,0.18)',
      display: 'flex', flexDirection: 'column',
      overflowY: 'auto',
      transform: shown ? 'translateX(0)' : 'translateX(100%)',
      transition: 'transform 0.28s cubic-bezier(0.32,0.72,0,1)',
    }}>{children}</div>
  );
}

// ─── TOP APP BAR (hamburger trigger) ─────────────────────────────────────────
function TopAppBar({ onMenuOpen, hasActiveRoutine }) {
  return (
    <div style={{
      position: 'absolute', top: 0, right: 0,
      padding: '14px 14px', zIndex: 30,
      pointerEvents: 'none',
    }}>
      <button
        onClick={onMenuOpen}
        style={{
          pointerEvents: 'auto',
          width: 38, height: 38, borderRadius: 11,
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer', boxShadow: 'var(--shadow-sm)',
          color: 'var(--text)', position: 'relative',
        }}
      >
        <div style={{ width: 18, height: 18 }}><Icon.Menu /></div>
      </button>
    </div>
  );
}

function BottomNav({ active, onNavigate, hasActiveRoutine }) {
  return (
    <nav className="bottom-nav">
      {NAV_TABS.map(tab => {
        const IconComp = Icon[tab.icon];
        const isActive = active === tab.id;
        return (
          <div key={tab.id} className={`nav-item ${isActive ? 'active' : ''}`} onClick={() => onNavigate(tab.id)}>
            <div className="nav-icon"><IconComp /></div>
            <span className="nav-label">{tab.label}</span>
            {tab.id === 'workout' && hasActiveRoutine && !isActive && <div className="nav-dot" />}
          </div>
        );
      })}
    </nav>
  );
}

// ─── SKELETON ────────────────────────────────────────────────────────────────
function SkeletonLine({ w = '100%', h = 16, mb = 8 }) {
  return <div className="skeleton" style={{ width: w, height: h, marginBottom: mb, borderRadius: 6 }} />;
}
function SkeletonCard({ rows = 3 }) {
  return (
    <div className="card" style={{ marginBottom: 12 }}>
      {Array.from({ length: rows }).map((_, i) => (
        <SkeletonLine key={i} w={i === 0 ? '60%' : i === rows-1 ? '40%' : '90%'} h={i === 0 ? 18 : 14} mb={i === rows-1 ? 0 : 10} />
      ))}
    </div>
  );
}

// ─── METRIC ──────────────────────────────────────────────────────────────────
function Metric({ value, label, delta, unit = '', mono = true }) {
  return (
    <div className="metric-card">
      <div className="metric-value" style={{ fontFamily: mono ? 'var(--font-mono)' : undefined }}>
        {value}<span style={{ fontSize: 14, fontWeight: 400, marginLeft: 3, color: 'var(--text2)' }}>{unit}</span>
      </div>
      <div className="metric-label">{label}</div>
      {delta != null && (
        <div className={`metric-delta ${delta > 0 ? 'delta-up' : delta < 0 ? 'delta-down' : 'delta-same'}`}>
          {delta > 0 ? '+' : ''}{delta}%
        </div>
      )}
    </div>
  );
}

// ─── TIMER RADIAL ────────────────────────────────────────────────────────────
function TimerRadial({ seconds, total, isRunning, onToggle, onReset, onClose, compact = false }) {
  const radius = compact ? 28 : 44;
  const stroke = compact ? 4 : 6;
  const circumference = 2 * Math.PI * radius;
  const progress = total > 0 ? (1 - seconds / total) : 0;
  const dashOffset = circumference * (1 - progress);
  const size = (radius + stroke + 2) * 2;
  const center = size / 2;

  return (
    <div style={{ display: 'flex', flexDirection: compact ? 'row' : 'column', alignItems: 'center', gap: compact ? 12 : 16 }}>
      <div style={{ position: 'relative', width: size, height: size }}>
        <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
          <circle cx={center} cy={center} r={radius} fill="none" stroke="var(--surface-alt)" strokeWidth={stroke} />
          <circle
            cx={center} cy={center} r={radius}
            fill="none"
            stroke={seconds <= 10 ? 'var(--error)' : 'var(--primary)'}
            strokeWidth={stroke}
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={dashOffset}
            style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.3s ease' }}
          />
        </svg>
        <div style={{
          position: 'absolute', inset: 0,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexDirection: 'column',
        }}>
          <span style={{
            fontFamily: 'var(--font-mono)', fontWeight: 600,
            fontSize: compact ? 13 : 22,
            color: seconds <= 10 ? 'var(--error)' : 'var(--text)',
            letterSpacing: '-0.5px',
          }}>{formatTime(seconds)}</span>
        </div>
      </div>
      {!compact && (
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-secondary btn-sm btn-icon" onClick={onToggle} style={{ padding: '10px 16px', gap: 6 }}>
            <div style={{ width: 16, height: 16 }}>{isRunning ? <Icon.Pause /> : <Icon.Play />}</div>
          </button>
          <button className="btn btn-ghost btn-sm" onClick={onReset}>Reset</button>
          <button className="btn btn-ghost btn-sm btn-icon" onClick={onClose} style={{ padding: '10px 12px' }}>
            <div style={{ width: 14, height: 14 }}><Icon.X /></div>
          </button>
        </div>
      )}
    </div>
  );
}

// ─── PR CONFETTI ─────────────────────────────────────────────────────────────
function PRCelebration({ onDone }) {
  const colors = ['var(--copper)', 'var(--primary)', '#8B7355', '#E8C547', 'var(--primary-dark)'];
  const particles = Array.from({ length: 18 }, (_, i) => ({
    id: i,
    color: colors[i % colors.length],
    left: 20 + (i / 18) * 60 + '%',
    delay: (i * 0.05) + 's',
    size: 4 + (i % 4),
  }));

  useEffect(() => {
    const t = setTimeout(onDone, 1800);
    return () => clearTimeout(t);
  }, []);

  return (
    <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', zIndex: 100, overflow: 'hidden' }}>
      {particles.map(p => (
        <div key={p.id} className="confetti-particle" style={{
          left: p.left, top: '10%',
          background: p.color,
          width: p.size, height: p.size,
          animationDelay: p.delay,
        }} />
      ))}
      <div style={{
        position: 'absolute', top: '40%', left: '50%', transform: 'translate(-50%, -50%)',
        background: 'var(--text)', color: 'var(--surface)',
        padding: '10px 24px', borderRadius: 'var(--radius-pill)',
        fontWeight: 700, fontSize: 15,
        fontFamily: 'var(--font-mono)', letterSpacing: 1,
        animation: 'fadeSlideUp 0.3s ease both',
      }}>
        — PR —
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: LAUNCH
// ═══════════════════════════════════════════════════════════════════════════
function LaunchScreen({ onDone }) {
  const [phase, setPhase] = useState(0);

  useEffect(() => {
    const t1 = setTimeout(() => setPhase(1), 300);
    const t2 = setTimeout(() => setPhase(2), 900);
    const t3 = setTimeout(() => onDone(), 2400);
    return () => { clearTimeout(t1); clearTimeout(t2); clearTimeout(t3); };
  }, []);

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      background: 'var(--bg)',
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      gap: 24,
    }}>
      {/* Logo mark */}
      <div style={{
        opacity: phase >= 1 ? 1 : 0,
        transform: phase >= 1 ? 'scale(1) translateY(0)' : 'scale(0.92) translateY(8px)',
        transition: 'opacity 0.5s ease, transform 0.5s cubic-bezier(0.34,1.56,0.64,1)',
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16,
      }}>
        <div style={{
          width: 72, height: 72,
          background: 'var(--primary)',
          borderRadius: 22,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 0 0 8px var(--primary-soft)',
        }}>
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
            <path d="M8 18h4v-6h4v12h4V12h4v12h4v-6" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.8px', color: 'var(--text)' }}>FitTrackPlus</div>
          <div style={{ fontSize: 13, color: 'var(--text2)', marginTop: 4, fontWeight: 400 }}>Registro. Progreso. Constancia.</div>
        </div>
      </div>

      {/* Progress bar */}
      <div style={{
        position: 'absolute', bottom: 80,
        width: 120, height: 2,
        background: 'var(--surface-alt)',
        borderRadius: 1, overflow: 'hidden',
        opacity: phase >= 2 ? 1 : 0,
        transition: 'opacity 0.3s ease',
      }}>
        <div style={{
          height: '100%', background: 'var(--primary)',
          borderRadius: 1,
          width: phase >= 2 ? '100%' : '0%',
          transition: 'width 1.2s cubic-bezier(0.4,0,0.2,1)',
        }} />
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: ONBOARDING
// ═══════════════════════════════════════════════════════════════════════════
function OnboardingScreen({ onDone }) {
  const [step, setStep] = useState(0);

  const steps = [
    {
      icon: (
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <rect x="8" y="12" width="48" height="40" rx="6" stroke="var(--primary)" strokeWidth="2"/>
          <line x1="8" y1="24" x2="56" y2="24" stroke="var(--primary)" strokeWidth="2"/>
          <rect x="16" y="32" width="10" height="12" rx="2" fill="var(--primary-soft)" stroke="var(--primary)" strokeWidth="1.5"/>
          <rect x="30" y="28" width="10" height="16" rx="2" fill="var(--primary)" />
          <rect x="44" y="34" width="10" height="10" rx="2" fill="var(--primary-soft)" stroke="var(--primary)" strokeWidth="1.5"/>
        </svg>
      ),
      title: 'Crea tus rutinas',
      desc: 'Organiza ejercicios por días. Push, Pull, Legs o lo que necesites. Tú controlas la estructura.',
    },
    {
      icon: (
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <circle cx="32" cy="32" r="22" stroke="var(--primary)" strokeWidth="2"/>
          <circle cx="32" cy="32" r="22" stroke="var(--primary-soft)" strokeWidth="8" strokeDasharray="90 48" strokeLinecap="round" transform="rotate(-90 32 32)"/>
          <text x="32" y="36" textAnchor="middle" fontFamily="'JetBrains Mono'" fontSize="14" fontWeight="600" fill="var(--text)">90s</text>
          <rect x="12" y="52" width="8" height="4" rx="1" fill="var(--primary)"/>
          <rect x="22" y="52" width="20" height="4" rx="1" fill="var(--surface-alt)"/>
          <rect x="44" y="52" width="8" height="4" rx="1" fill="var(--surface-alt)"/>
        </svg>
      ),
      title: 'Registra sin fricción',
      desc: 'Anota peso y reps por serie. El timer de descanso arranca solo. Tus datos quedan guardados localmente.',
    },
    {
      icon: (
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <polyline points="8,48 18,32 28,38 38,22 48,28 58,12" stroke="var(--primary)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
          <circle cx="18" cy="32" r="3" fill="var(--primary)"/>
          <circle cx="38" cy="22" r="3" fill="var(--primary)"/>
          <circle cx="58" cy="12" r="3" fill="var(--copper)"/>
          <rect x="8" y="50" width="48" height="2" rx="1" fill="var(--surface-alt)"/>
        </svg>
      ),
      title: 'Analiza tu progreso',
      desc: 'Historial inmutable, snapshots de sesión, gráficas de evolución. Todo sin cuenta, sin cloud, sin ruido.',
    },
  ];

  const current = steps[step];

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 100,
      background: 'var(--bg)',
      display: 'flex', flexDirection: 'column',
    }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px 32px', gap: 32 }}>
        <div key={step} className="anim-fade-up" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 28 }}>
          <div style={{
            width: 120, height: 120,
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 32,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            {current.icon}
          </div>
          <div style={{ textAlign: 'center', maxWidth: 300 }}>
            <div className="t-title" style={{ marginBottom: 12 }}>{current.title}</div>
            <div className="t-body t-secondary">{current.desc}</div>
          </div>
        </div>

        {/* Dots */}
        <div style={{ display: 'flex', gap: 8 }}>
          {steps.map((_, i) => (
            <div key={i} style={{
              width: i === step ? 20 : 6, height: 6,
              borderRadius: 3,
              background: i === step ? 'var(--primary)' : 'var(--surface-alt2)',
              transition: 'width 0.3s ease, background 0.3s ease',
              cursor: 'pointer',
            }} onClick={() => setStep(i)} />
          ))}
        </div>
      </div>

      <div style={{ padding: '0 24px 48px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        {step < steps.length - 1 ? (
          <>
            <button className="btn btn-primary btn-full" onClick={() => setStep(s => s + 1)}>
              Continuar
            </button>
            <button className="btn btn-ghost btn-full t-secondary" style={{ fontSize: 14 }} onClick={onDone}>
              Saltar intro
            </button>
          </>
        ) : (
          <button className="btn btn-primary btn-full" onClick={onDone} style={{ fontSize: 17, padding: '16px' }}>
            Empezar
          </button>
        )}
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: HOME
// ═══════════════════════════════════════════════════════════════════════════
function HomeScreen({ onNavigate, routines, sessions, loading, onMenuOpen }) {
  const [skelDone, setSkelDone] = useState(false);
  useEffect(() => {
    if (loading) return;
    const t = setTimeout(() => setSkelDone(true), 600);
    return () => clearTimeout(t);
  }, [loading]);

  const activeRoutine = routines.find(r => r.isActive);
  const sessionsThisWeek = M.getSessionsThisWeek();
  const totalSessions = sessions.length;
  const nextDay = M.getNextWorkoutDay();
  const lastSession = sessions[0];

  // Week day strip — Mon of current week
  const today = new Date('2026-05-03');
  const weekDays = ['L','M','X','J','V','S','D'];
  const todayDow = today.getDay(); // 0=Sun
  const mondayOffset = todayDow === 0 ? -6 : 1 - todayDow;
  const monday = new Date(today);
  monday.setDate(today.getDate() + mondayOffset);
  const weekDates = Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    return d;
  });
  const sessionDates = new Set(sessions.map(s => s.date.slice(0, 10)));

  if (!skelDone) {
    return (
      <div className="screen" style={{ padding: '20px' }}>
        <SkeletonLine w="50%" h={28} mb={20} />
        <SkeletonCard rows={4} />
        <SkeletonCard rows={2} />
        <SkeletonCard rows={3} />
      </div>
    );
  }

  return (
    <div className="screen anim-fade-up">
      {/* Header */}
      <div style={{ padding: '20px 20px 8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
          <div style={{ fontSize: 13, color: 'var(--text2)', fontWeight: 500 }}>
            {today.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' })}
          </div>
          {onMenuOpen && (
            <button className="btn btn-ghost btn-icon" onClick={onMenuOpen} style={{ color: 'var(--text2)', padding: 6 }}>
              <div style={{ width: 20, height: 20 }}><Icon.Menu /></div>
            </button>
          )}
        </div>
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.6px' }}>Buenos días.</div>
      </div>

      {/* Week strip */}
      <div style={{ marginTop: 16, marginBottom: 8 }}>
        <div className="week-strip">
          {weekDates.map((d, i) => {
            const key = d.toISOString().slice(0, 10);
            const isDone = sessionDates.has(key);
            const isToday = key === '2026-05-03';
            return (
              <div key={i} className={`week-day ${isToday ? 'today' : isDone ? 'done' : ''}`}>
                <span className="wd-label">{weekDays[i]}</span>
                <span className="wd-num" style={{ fontFamily: 'var(--font-mono)' }}>{d.getDate()}</span>
                {isDone && !isToday && (
                  <div style={{ width: 4, height: 4, borderRadius: 2, background: 'var(--primary)', marginTop: 1 }} />
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Active routine / CTA */}
      <div style={{ padding: '8px 16px' }}>
        {activeRoutine ? (
          <div className="card" style={{
            background: 'var(--primary)', border: 'none',
            position: 'relative', overflow: 'hidden',
          }}>
            <div style={{ position: 'absolute', top: -20, right: -20, width: 100, height: 100, borderRadius: '50%', background: 'rgba(255,255,255,0.06)' }} />
            <div style={{ position: 'absolute', bottom: -30, left: 40, width: 80, height: 80, borderRadius: '50%', background: 'rgba(255,255,255,0.04)' }} />
            <div style={{ position: 'relative' }}>
              <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: 1, textTransform: 'uppercase', color: 'rgba(255,255,255,0.65)', marginBottom: 6 }}>Rutina activa</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: '#fff', letterSpacing: '-0.4px', marginBottom: 2 }}>{activeRoutine.name}</div>
              {nextDay && (
                <div style={{ fontSize: 14, color: 'rgba(255,255,255,0.75)', marginBottom: 16 }}>
                  Siguiente: <span style={{ fontWeight: 600, color: '#fff' }}>{nextDay.label}</span> · {nextDay.exercises.length} ejercicios
                </div>
              )}
              <button
                className="btn"
                style={{ background: '#fff', color: 'var(--primary)', fontWeight: 700, padding: '12px 24px', borderRadius: 'var(--radius-md)' }}
                onClick={() => onNavigate('workout')}
              >
                <div style={{ width: 18, height: 18 }}><Icon.Play /></div>
                Comenzar entrenamiento
              </button>
            </div>
          </div>
        ) : (
          <div className="card" style={{ textAlign: 'center' }}>
            <div style={{ width: 44, height: 44, borderRadius: 12, background: 'var(--primary-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px' }}>
              <div style={{ width: 24, height: 24, color: 'var(--primary)' }}><Icon.Dumbbell /></div>
            </div>
            <div style={{ fontWeight: 600, marginBottom: 6 }}>Sin rutina activa</div>
            <div className="t-small t-secondary" style={{ marginBottom: 16 }}>Crea o activa una rutina para empezar a registrar tus entrenamientos.</div>
            <button className="btn btn-primary" onClick={() => onNavigate('routines')}>Ir a Rutinas</button>
          </div>
        )}
      </div>

      {/* Stats row */}
      <div style={{ padding: '4px 16px 8px' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <Metric value={sessionsThisWeek} label="Sesiones esta semana" unit="/ 5" />
          <Metric value={totalSessions} label="Total sesiones" />
        </div>
      </div>

      {/* Last session */}
      {lastSession && (
        <div style={{ padding: '4px 16px 8px' }}>
          <div className="section-label" style={{ padding: '0 0 8px' }}>Última sesión</div>
          <div className="card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('history')}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 2 }}>{lastSession.dayLabel}</div>
                <div className="t-small t-secondary">{lastSession.routineName} · {formatDate(lastSession.date)}</div>
              </div>
              <span className="badge badge-primary">{lastSession.durationMinutes} min</span>
            </div>
            <div style={{ display: 'flex', gap: 20 }}>
              <div>
                <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, fontSize: 18, color: 'var(--text)', letterSpacing: '-0.5px' }}>{formatVolume(lastSession.volume)}<span style={{ fontSize: 12, marginLeft: 2, color: 'var(--text2)', fontFamily: 'inherit' }}>kg</span></div>
                <div className="t-tiny t-secondary">Volumen</div>
              </div>
              <div>
                <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, fontSize: 18, color: 'var(--text)', letterSpacing: '-0.5px' }}>{lastSession.totalSets}</div>
                <div className="t-tiny t-secondary">Series</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick links */}
      <div style={{ padding: '4px 16px 24px' }}>
        <div className="section-label" style={{ padding: '0 0 8px' }}>Accesos rápidos</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {[
            { label: 'Rutinas', icon: 'Dumbbell', tab: 'routines', sub: `${routines.filter(r=>!r.isArchived).length} activas` },
            { label: 'Historial', icon: 'History', tab: 'history', sub: `${totalSessions} sesiones` },
            { label: 'Estadísticas', icon: 'BarChart', tab: 'stats', sub: 'Progreso y marcas' },
            { label: 'Widget', icon: 'Widget', tab: 'widget', sub: 'Vista Android' },
          ].map(item => {
            const IcoC = Icon[item.icon];
            return (
              <div key={item.tab} className="card" style={{ cursor: 'pointer', display: 'flex', flexDirection: 'column', gap: 8 }} onClick={() => onNavigate(item.tab)}>
                <div style={{ width: 32, height: 32, borderRadius: 8, background: 'var(--primary-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <div style={{ width: 18, height: 18, color: 'var(--primary)' }}><IcoC /></div>
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{item.label}</div>
                  <div className="t-tiny t-secondary">{item.sub}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: ROUTINES
// ═══════════════════════════════════════════════════════════════════════════
function RoutinesScreen({ routines: initialRoutines, onRoutinesChange, showToast, onMenuOpen }) {
  const [routinesList, setRoutinesList] = useState(initialRoutines);
  const [view, setView] = useState('list'); // list | editor | archived
  const [editingRoutine, setEditingRoutine] = useState(null);
  const [showArchived, setShowArchived] = useState(false);
  const [openMenuId, setOpenMenuId] = useState(null);

  const active = routinesList.filter(r => !r.isArchived);
  const archived = routinesList.filter(r => r.isArchived);

  function activateRoutine(id) {
    const updated = routinesList.map(r => ({ ...r, isActive: r.id === id }));
    setRoutinesList(updated);
    onRoutinesChange(updated);
    showToast('Rutina activada');
    setOpenMenuId(null);
  }

  function archiveRoutine(id) {
    const updated = routinesList.map(r => r.id === id ? { ...r, isArchived: true, isActive: false } : r);
    setRoutinesList(updated);
    onRoutinesChange(updated);
    showToast('Rutina archivada');
    setOpenMenuId(null);
  }

  function restoreRoutine(id) {
    const updated = routinesList.map(r => r.id === id ? { ...r, isArchived: false } : r);
    setRoutinesList(updated);
    onRoutinesChange(updated);
    showToast('Rutina restaurada');
  }

  function openEditor(routine) {
    setEditingRoutine(routine ? JSON.parse(JSON.stringify(routine)) : {
      id: `routine_${Date.now()}`,
      name: '', isActive: false, isArchived: false,
      createdAt: new Date().toISOString().slice(0, 10),
      updatedAt: new Date().toISOString().slice(0, 10),
      days: [],
    });
    setView('editor');
  }

  function saveRoutine(r) {
    const exists = routinesList.find(x => x.id === r.id);
    const updated = exists ? routinesList.map(x => x.id === r.id ? r : x) : [...routinesList, r];
    setRoutinesList(updated);
    onRoutinesChange(updated);
    setView('list');
    showToast('Rutina guardada');
  }

  if (view === 'editor') {
    return <RoutineEditor routine={editingRoutine} onSave={saveRoutine} onBack={() => setView('list')} />;
  }

  return (
    <div className="screen anim-fade-up" onClick={() => setOpenMenuId(null)}>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {onMenuOpen && (
            <button className="btn btn-ghost btn-icon" onClick={onMenuOpen} style={{ color: 'var(--text2)' }}>
              <div style={{ width: 20, height: 20 }}><Icon.Menu /></div>
            </button>
          )}
          <div className="page-header-title">Rutinas</div>
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => openEditor(null)} style={{ gap: 4 }}>
          <div style={{ width: 16, height: 16 }}><Icon.Plus /></div>
          Nueva
        </button>
      </div>

      {/* Templates strip */}
      <div style={{ padding: '0 16px 4px' }}>
        <div className="section-label" style={{ padding: '0 0 8px' }}>Plantillas</div>
        <div style={{ display: 'flex', gap: 8, overflowX: 'auto', paddingBottom: 4, scrollbarWidth: 'none' }}>
          {['Push Pull Legs', 'Upper Lower', 'Full Body'].map(name => (
            <div key={name} style={{
              flexShrink: 0, padding: '8px 14px',
              background: 'var(--surface)', border: '1px solid var(--border)',
              borderRadius: 'var(--radius-sm)', cursor: 'pointer',
              fontSize: 13, fontWeight: 500,
              whiteSpace: 'nowrap',
            }} onClick={() => showToast(`Plantilla "${name}" cargada`)}>
              {name}
            </div>
          ))}
        </div>
      </div>

      {/* Active routines */}
      <div style={{ padding: '8px 16px' }}>
        <div className="section-label" style={{ padding: '0 0 8px' }}>Activas · {active.length}</div>
        {active.length === 0 && (
          <div className="empty-state">
            <div className="t-secondary t-small">Sin rutinas activas. Crea una nueva o restaura una archivada.</div>
          </div>
        )}
        {active.map(r => (
          <RoutineCard
            key={r.id} routine={r}
            menuOpen={openMenuId === r.id}
            onMenuToggle={(e) => { e.stopPropagation(); setOpenMenuId(openMenuId === r.id ? null : r.id); }}
            onActivate={() => activateRoutine(r.id)}
            onEdit={() => openEditor(r)}
            onArchive={() => archiveRoutine(r.id)}
          />
        ))}
      </div>

      {/* Archived */}
      {archived.length > 0 && (
        <div style={{ padding: '0 16px 24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 0 8px' }}>
            <div className="section-label" style={{ padding: 0 }}>Archivadas · {archived.length}</div>
            <button className="btn btn-ghost btn-sm t-secondary" style={{ fontSize: 12 }} onClick={() => setShowArchived(v => !v)}>
              {showArchived ? 'Ocultar' : 'Mostrar'}
            </button>
          </div>
          {showArchived && archived.map(r => (
            <div key={r.id} className="card" style={{ marginBottom: 10, opacity: 0.7 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 15 }}>{r.name}</div>
                  <div className="t-tiny t-secondary">{r.days.length} días · archivada</div>
                </div>
                <button className="btn btn-ghost btn-sm" style={{ fontSize: 12 }} onClick={() => restoreRoutine(r.id)}>
                  Restaurar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function RoutineCard({ routine, menuOpen, onMenuToggle, onActivate, onEdit, onArchive }) {
  return (
    <div className="card" style={{ marginBottom: 10, position: 'relative' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <span style={{ fontWeight: 700, fontSize: 16, letterSpacing: '-0.3px' }}>{routine.name}</span>
            {routine.isActive && <span className="badge badge-primary">Activa</span>}
          </div>
          <div className="t-tiny t-secondary">{routine.days.length} días · {routine.days.reduce((a, d) => a + d.exercises.length, 0)} ejercicios</div>
        </div>
        <button className="btn btn-ghost btn-sm btn-icon" onClick={onMenuToggle} style={{ padding: '6px', position: 'relative' }}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="5" r="2"/><circle cx="12" cy="12" r="2"/><circle cx="12" cy="19" r="2"/></svg>
        </button>
      </div>

      {/* Days preview */}
      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 10 }}>
        {routine.days.map(d => (
          <span key={d.id} style={{
            padding: '3px 8px', background: 'var(--primary-subtle)',
            color: 'var(--primary)', borderRadius: 'var(--radius-pill)',
            fontSize: 11, fontWeight: 600,
          }}>{d.label}</span>
        ))}
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={onEdit}>Editar</button>
        {!routine.isActive && (
          <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={onActivate}>Activar</button>
        )}
      </div>

      {/* Dropdown menu */}
      {menuOpen && (
        <div style={{
          position: 'absolute', top: 40, right: 16, zIndex: 30,
          background: 'var(--surface)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-md)', padding: '4px',
          boxShadow: 'var(--shadow-lg)', minWidth: 160,
        }} onClick={e => e.stopPropagation()}>
          <div className="settings-row" style={{ padding: '10px 14px', cursor: 'pointer', border: 'none' }} onClick={onEdit}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <div style={{ width: 16, height: 16, color: 'var(--text2)' }}><Icon.Edit /></div>
              <span style={{ fontSize: 14 }}>Editar</span>
            </div>
          </div>
          <div className="settings-row" style={{ padding: '10px 14px', cursor: 'pointer', border: 'none' }} onClick={() => {}}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <div style={{ width: 16, height: 16, color: 'var(--text2)' }}><Icon.Duplicate /></div>
              <span style={{ fontSize: 14 }}>Duplicar</span>
            </div>
          </div>
          <div className="divider" />
          <div className="settings-row" style={{ padding: '10px 14px', cursor: 'pointer', border: 'none' }} onClick={onArchive}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <div style={{ width: 16, height: 16, color: 'var(--text2)' }}><Icon.Archive /></div>
              <span style={{ fontSize: 14, color: 'var(--text2)' }}>Archivar</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── ROUTINE EDITOR ───────────────────────────────────────────────────────────
function RoutineEditor({ routine, onSave, onBack }) {
  const [data, setData] = useState(routine);
  const [errors, setErrors] = useState({});
  const [expandedDay, setExpandedDay] = useState(data.days[0]?.id || null);
  const exList = Object.values(M.exercises);

  function validate() {
    const e = {};
    if (!data.name.trim()) e.name = 'El nombre es obligatorio';
    if (data.days.length === 0) e.days = 'Añade al menos un día';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  function addDay() {
    const newDay = { id: `day_${Date.now()}`, label: `Día ${data.days.length + 1}`, dayOfWeek: '', exercises: [] };
    setData(d => ({ ...d, days: [...d.days, newDay] }));
    setExpandedDay(newDay.id);
  }

  function removeDay(dayId) {
    setData(d => ({ ...d, days: d.days.filter(x => x.id !== dayId) }));
  }

  function updateDay(dayId, field, value) {
    setData(d => ({ ...d, days: d.days.map(x => x.id === dayId ? { ...x, [field]: value } : x) }));
  }

  function addExercise(dayId) {
    const ex = exList[0];
    const newEx = { exerciseId: ex.id, sets: 3, reps: '8-12', weight: 20, notes: '' };
    setData(d => ({ ...d, days: d.days.map(x => x.id === dayId ? { ...x, exercises: [...x.exercises, newEx] } : x) }));
  }

  function removeExercise(dayId, idx) {
    setData(d => ({ ...d, days: d.days.map(x => x.id === dayId ? { ...x, exercises: x.exercises.filter((_, i) => i !== idx) } : x) }));
  }

  function updateExercise(dayId, idx, field, value) {
    setData(d => ({ ...d, days: d.days.map(x => x.id === dayId ? { ...x, exercises: x.exercises.map((e, i) => i === idx ? { ...e, [field]: value } : e) } : x) }));
  }

  return (
    <div className="screen anim-fade-up">
      {/* Header */}
      <div style={{ padding: '16px 16px 8px', display: 'flex', alignItems: 'center', gap: 12, position: 'sticky', top: 0, background: 'var(--bg)', zIndex: 10 }}>
        <button className="btn btn-ghost btn-icon" onClick={onBack}><div style={{ width: 20, height: 20 }}><Icon.ChevronLeft /></div></button>
        <div style={{ flex: 1, fontWeight: 700, fontSize: 18, letterSpacing: '-0.3px' }}>
          {data.name || 'Nueva rutina'}
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => validate() && onSave(data)}>Guardar</button>
      </div>

      <div style={{ padding: '8px 16px' }}>
        {/* Name */}
        <div style={{ marginBottom: 16 }}>
          <div className="t-label t-secondary" style={{ marginBottom: 6 }}>Nombre</div>
          <input
            className="input"
            value={data.name}
            onChange={e => setData(d => ({ ...d, name: e.target.value }))}
            placeholder="Ej. Push Pull Legs"
            style={{ borderColor: errors.name ? 'var(--error)' : undefined }}
          />
          {errors.name && <div style={{ fontSize: 12, color: 'var(--error)', marginTop: 4 }}>{errors.name}</div>}
        </div>

        {/* Days */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <div className="t-label t-secondary">Días · {data.days.length}</div>
          <button className="btn btn-ghost btn-sm" style={{ gap: 4, fontSize: 13 }} onClick={addDay}>
            <div style={{ width: 14, height: 14 }}><Icon.Plus /></div>
            Añadir día
          </button>
        </div>
        {errors.days && <div style={{ fontSize: 12, color: 'var(--error)', marginBottom: 8 }}>{errors.days}</div>}

        {data.days.map((day, di) => (
          <div key={day.id} className="card" style={{ marginBottom: 10 }}>
            {/* Day header */}
            <div
              style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: expandedDay === day.id ? 12 : 0, cursor: 'pointer' }}
              onClick={() => setExpandedDay(expandedDay === day.id ? null : day.id)}
            >
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600, fontSize: 15 }}>{day.label || `Día ${di+1}`}</div>
                <div className="t-tiny t-secondary">{day.exercises.length} ejercicios</div>
              </div>
              <button className="btn btn-ghost btn-sm btn-icon" style={{ padding: 6 }} onClick={e => { e.stopPropagation(); removeDay(day.id); }}>
                <div style={{ width: 16, height: 16 }}><Icon.Trash /></div>
              </button>
              <div style={{ width: 16, height: 16, color: 'var(--text3)', transform: expandedDay === day.id ? 'rotate(180deg)' : 'none', transition: '0.2s' }}>
                <Icon.ChevronDown />
              </div>
            </div>

            {expandedDay === day.id && (
              <div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 12 }}>
                  <input className="input" style={{ fontSize: 13 }} value={day.label} onChange={e => updateDay(day.id, 'label', e.target.value)} placeholder="Nombre del día" />
                  <input className="input" style={{ fontSize: 13 }} value={day.dayOfWeek} onChange={e => updateDay(day.id, 'dayOfWeek', e.target.value)} placeholder="Día semana" />
                </div>

                {day.exercises.map((ex, ei) => {
                  const exDef = M.exercises[ex.exerciseId] || exList.find(e => e.id === ex.exerciseId);
                  return (
                    <div key={ei} style={{ background: 'var(--bg)', borderRadius: 8, padding: 10, marginBottom: 8 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                        <select
                          className="input"
                          style={{ flex: 1, fontSize: 13, padding: '8px 10px' }}
                          value={ex.exerciseId}
                          onChange={e => updateExercise(day.id, ei, 'exerciseId', e.target.value)}
                        >
                          {exList.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
                        </select>
                        <button className="btn btn-ghost btn-sm btn-icon" style={{ padding: 6, flexShrink: 0 }} onClick={() => removeExercise(day.id, ei)}>
                          <div style={{ width: 14, height: 14 }}><Icon.X /></div>
                        </button>
                      </div>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 6 }}>
                        <div>
                          <div style={{ fontSize: 10, fontWeight: 600, color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4 }}>Series</div>
                          <input className="input input-mono" style={{ fontSize: 14, padding: '7px' }} type="number" min={1} max={10} value={ex.sets} onChange={e => updateExercise(day.id, ei, 'sets', parseInt(e.target.value)||1)} />
                        </div>
                        <div>
                          <div style={{ fontSize: 10, fontWeight: 600, color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4 }}>Reps</div>
                          <input className="input input-mono" style={{ fontSize: 14, padding: '7px' }} value={ex.reps} onChange={e => updateExercise(day.id, ei, 'reps', e.target.value)} placeholder="8-12" />
                        </div>
                        <div>
                          <div style={{ fontSize: 10, fontWeight: 600, color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4 }}>Peso kg</div>
                          <input className="input input-mono" style={{ fontSize: 14, padding: '7px' }} type="number" step={0.5} min={0} value={ex.weight} onChange={e => updateExercise(day.id, ei, 'weight', parseFloat(e.target.value)||0)} />
                        </div>
                      </div>
                      <input className="input" style={{ marginTop: 6, fontSize: 12, padding: '6px 10px' }} value={ex.notes} onChange={e => updateExercise(day.id, ei, 'notes', e.target.value)} placeholder="Notas opcionales…" />
                    </div>
                  );
                })}

                <button className="btn btn-ghost btn-sm btn-full" style={{ gap: 6, marginTop: 4, border: '1px dashed var(--border)', borderRadius: 8 }} onClick={() => addExercise(day.id)}>
                  <div style={{ width: 14, height: 14 }}><Icon.Plus /></div>
                  Añadir ejercicio
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: WORKOUT
// ═══════════════════════════════════════════════════════════════════════════
function WorkoutScreen({ routines, sessions, onSessionComplete, showToast, onMenuOpen }) {
  const [phase, setPhase] = useState('preview'); // preview | active | finishing | summary | cancel-confirm
  const [activeSession, setActiveSession] = useState(null);
  const [timerSecs, setTimerSecs] = useState(0);
  const [timerTotal, setTimerTotal] = useState(90);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerExpanded, setTimerExpanded] = useState(false);
  const [autoStart, setAutoStart] = useState(true);
  const [prModal, setPrModal] = useState(null); // { exerciseId, weight, reps, prevWeight, prevReps }
  const [elapsedSecs, setElapsedSecs] = useState(0);
  const timerRef = useRef(null);
  const elapsedRef = useRef(null);

  const activeRoutine = routines.find(r => r.isActive);
  const nextDay = M.getNextWorkoutDay();
  const bestSets = M.getBestSets();

  // Rest timer countdown
  useEffect(() => {
    if (timerRunning && timerSecs > 0) {
      timerRef.current = setTimeout(() => setTimerSecs(s => s - 1), 1000);
    } else if (timerSecs === 0 && timerRunning) {
      setTimerRunning(false);
    }
    return () => clearTimeout(timerRef.current);
  }, [timerRunning, timerSecs]);

  // Elapsed session clock
  useEffect(() => {
    if (phase === 'active') {
      elapsedRef.current = setInterval(() => setElapsedSecs(s => s + 1), 1000);
    }
    return () => clearInterval(elapsedRef.current);
  }, [phase]);

  function startTimer(dur) {
    setTimerTotal(dur); setTimerSecs(dur); setTimerRunning(true);
    setTimerExpanded(false);
  }

  function startWorkout() {
    if (!nextDay) return;
    const sets = nextDay.exercises.flatMap(ex =>
      Array.from({ length: ex.sets }, (_, i) => ({
        id: `${ex.exerciseId}_set${i}`,
        exerciseId: ex.exerciseId,
        setNum: i + 1,
        targetReps: ex.reps,
        suggestedWeight: ex.weight,
        weight: ex.weight,
        reps: typeof ex.reps === 'number' ? ex.reps : parseInt(ex.reps) || 8,
        completed: false,
        isPR: false,
        notes: '',
      }))
    );
    setActiveSession({
      routineId: activeRoutine.id,
      dayId: nextDay.id,
      dayLabel: nextDay.label,
      routineName: activeRoutine.name,
      startTime: Date.now(),
      sets,
      notes: '',
    });
    setElapsedSecs(0);
    setPhase('active');
  }

  function completeSet(setId) {
    let newPR = null;
    setActiveSession(s => {
      const updatedSets = s.sets.map(set => {
        if (set.id !== setId || set.completed) return set;
        const prev = bestSets[set.exerciseId];
        const isPR = prev ? (set.weight * set.reps > prev.weight * prev.reps) : set.weight > 0;
        if (isPR) {
          newPR = {
            exerciseId: set.exerciseId,
            exName: Object.values(M.exercises).find(e => e.id === set.exerciseId)?.name || set.exerciseId,
            weight: set.weight,
            reps: set.reps,
            prevWeight: prev?.weight || 0,
            prevReps: prev?.reps || 0,
          };
        }
        return { ...set, completed: true, isPR };
      });
      return { ...s, sets: updatedSets };
    });
    if (newPR) setPrModal(newPR);
    if (autoStart) startTimer(timerTotal);
  }

  function updateSetField(setId, field, value) {
    setActiveSession(s => ({
      ...s,
      sets: s.sets.map(set => set.id === setId ? { ...set, [field]: value } : set)
    }));
  }

  function buildSession() {
    const dur = Math.max(Math.round(elapsedSecs / 60), 1);
    const completedSets = activeSession.sets.filter(s => s.completed);
    const volume = completedSets.reduce((a, s) => a + s.weight * s.reps, 0);
    return {
      id: `session_${Date.now()}`,
      routineId: activeSession.routineId,
      dayId: activeSession.dayId,
      dayLabel: activeSession.dayLabel,
      routineName: activeSession.routineName,
      date: new Date().toISOString(),
      durationMinutes: dur,
      sets: completedSets,
      volume,
      notes: activeSession.notes,
      totalSets: completedSets.length,
    };
  }

  function saveAndExit() {
    const session = buildSession();
    onSessionComplete(session);
    setPhase('summary');
    setActiveSession(prev => ({ ...prev, _saved: session }));
    clearInterval(elapsedRef.current);
    clearTimeout(timerRef.current);
    setTimerRunning(false);
  }

  function discardAndExit() {
    setPhase('preview');
    setActiveSession(null);
    setElapsedSecs(0);
    setTimerSecs(0);
    setTimerRunning(false);
    showToast('Entrenamiento descartado');
  }

  // ── No active routine ─────────────────────────────────────────────────
  if (!activeRoutine) {
    return (
      <div className="screen" style={{ display: 'flex', flexDirection: 'column' }}>
        <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {onMenuOpen && (
            <button className="btn btn-ghost btn-icon" onClick={onMenuOpen} style={{ color: 'var(--text2)', padding: 6 }}>
              <div style={{ width: 20, height: 20 }}><Icon.Menu /></div>
            </button>
          )}
          <div className="page-header-title">Entrenar</div>
        </div>
        <div className="empty-state" style={{ flex: 1 }}>
          <div style={{ width: 56, height: 56, borderRadius: 16, background: 'var(--primary-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ width: 32, height: 32, color: 'var(--primary)' }}><Icon.Dumbbell /></div>
          </div>
          <div style={{ fontWeight: 700, fontSize: 18 }}>Sin rutina activa</div>
          <div className="t-secondary t-small" style={{ maxWidth: 260, textAlign: 'center' }}>
            Ve a Rutinas, activa una y vuelve aquí para empezar a entrenar.
          </div>
        </div>
      </div>
    );
  }

  // ── Preview ────────────────────────────────────────────────────────────
  if (phase === 'preview') {
    return (
      <div className="screen anim-fade-up">
        <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {onMenuOpen && (
            <button className="btn btn-ghost btn-icon" onClick={onMenuOpen} style={{ color: 'var(--text2)', padding: 6 }}>
              <div style={{ width: 20, height: 20 }}><Icon.Menu /></div>
            </button>
          )}
          <div className="page-header-title">Entrenar</div>
        </div>
        <div style={{ padding: '0 16px' }}>
          {nextDay ? (
            <>
              <div style={{ marginBottom: 16 }}>
                <span className="badge badge-primary" style={{ marginBottom: 10, display: 'inline-flex' }}>{activeRoutine.name}</span>
                <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.6px', marginBottom: 4 }}>{nextDay.label}</div>
                <div className="t-small t-secondary">{nextDay.exercises.length} ejercicios · {nextDay.exercises.reduce((a, e) => a + e.sets, 0)} series estimadas</div>
              </div>

              {/* Exercise list */}
              <div className="card" style={{ marginBottom: 16, padding: 0, overflow: 'hidden' }}>
                {nextDay.exercises.map((ex, i) => {
                  const exDef = Object.values(M.exercises).find(e => e.id === ex.exerciseId);
                  const best = bestSets[ex.exerciseId];
                  return (
                    <div key={i} style={{
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                      padding: '12px 16px',
                      borderBottom: i < nextDay.exercises.length - 1 ? '1px solid var(--border)' : 'none',
                    }}>
                      <div>
                        <div style={{ fontSize: 14, fontWeight: 600 }}>{exDef?.name || ex.exerciseId}</div>
                        <div style={{ fontSize: 12, color: 'var(--text3)' }}>
                          {exDef?.muscleGroup}
                          {best && <span style={{ fontFamily: 'var(--font-mono)', marginLeft: 8 }}>· Mejor {best.weight}kg×{best.reps}</span>}
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontFamily: 'var(--font-mono)', fontSize: 14, fontWeight: 700 }}>{ex.sets}×{ex.reps}</div>
                        {ex.weight > 0 && <div style={{ fontSize: 11, color: 'var(--text3)', fontFamily: 'var(--font-mono)' }}>{ex.weight} kg</div>}
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Timer default + autostart */}
              <div className="card" style={{ marginBottom: 16, padding: '14px 16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>Timer de descanso</div>
                    <div className="t-tiny t-secondary">Duración por defecto</div>
                  </div>
                  <div style={{ display: 'flex', gap: 6 }}>
                    {[60, 90, 120].map(d => (
                      <button key={d} onClick={() => setTimerTotal(d)} style={{
                        padding: '6px 12px', borderRadius: 8, border: 'none',
                        background: d === timerTotal ? 'var(--primary)' : 'var(--surface-alt)',
                        color: d === timerTotal ? '#fff' : 'var(--text2)',
                        fontFamily: 'var(--font-mono)', fontSize: 12, fontWeight: 600, cursor: 'pointer',
                        transition: 'var(--transition)',
                      }}>{d}s</button>
                    ))}
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <div>
                    <div style={{ fontWeight: 500, fontSize: 14 }}>Inicio automático</div>
                    <div className="t-tiny t-secondary">Al completar cada serie</div>
                  </div>
                  <button className={`toggle ${autoStart ? 'on' : ''}`} onClick={() => setAutoStart(v => !v)} />
                </div>
              </div>

              <button className="btn btn-primary btn-full" style={{ padding: '16px', fontSize: 16, letterSpacing: '-0.2px' }} onClick={startWorkout}>
                <div style={{ width: 20, height: 20 }}><Icon.Play /></div>
                Comenzar {nextDay.label}
              </button>
            </>
          ) : (
            <div className="empty-state">
              <div className="t-secondary t-small">No hay día siguiente configurado en tu rutina.</div>
            </div>
          )}
        </div>
      </div>
    );
  }

  // ── Active session (also handles cancel-confirm overlay) ─────────────
  if ((phase === 'active' || phase === 'cancel-confirm') && activeSession) {
    const exerciseGroups = nextDay.exercises.map(ex => ({
      ...ex,
      exDef: Object.values(M.exercises).find(e => e.id === ex.exerciseId),
      sets: activeSession.sets.filter(s => s.exerciseId === ex.exerciseId),
    }));
    const totalSets = activeSession.sets.length;
    const completedCount = activeSession.sets.filter(s => s.completed).length;
    const progress = totalSets > 0 ? completedCount / totalSets : 0;
    const elapsedMin = Math.floor(elapsedSecs / 60);
    const elapsedSecRem = elapsedSecs % 60;
    const elapsedDisplay = `${elapsedMin}:${elapsedSecRem.toString().padStart(2,'0')}`;
    const timerActive = timerRunning || timerSecs > 0;

    return (
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column', position: 'relative' }}>

        {/* PR Modal */}
        {prModal && (
          <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.55)', zIndex: 90, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}
            onClick={() => setPrModal(null)}>
            <div className="anim-fade-up" style={{ background: 'var(--surface)', borderRadius: 20, padding: '28px 24px', width: '100%', maxWidth: 320, textAlign: 'center' }}
              onClick={e => e.stopPropagation()}>
              <PRCelebration onDone={() => {}} />
              <div style={{ position: 'relative', zIndex: 2 }}>
                <div style={{ fontSize: 12, fontWeight: 700, letterSpacing: 2, textTransform: 'uppercase', color: 'var(--copper)', marginBottom: 12 }}>Nuevo récord personal</div>
                <div style={{ fontWeight: 700, fontSize: 18, marginBottom: 20 }}>{prModal.exName}</div>
                <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginBottom: 20 }}>
                  {prModal.prevWeight > 0 && (
                    <div style={{ padding: '10px 16px', background: 'var(--surface-alt)', borderRadius: 10, textAlign: 'center' }}>
                      <div style={{ fontFamily: 'var(--font-mono)', fontSize: 20, fontWeight: 600, color: 'var(--text2)' }}>{prModal.prevWeight}kg×{prModal.prevReps}</div>
                      <div style={{ fontSize: 11, color: 'var(--text3)', marginTop: 2 }}>anterior</div>
                    </div>
                  )}
                  <div style={{ padding: '10px 16px', background: 'var(--copper-soft)', border: '2px solid var(--copper)', borderRadius: 10, textAlign: 'center' }}>
                    <div style={{ fontFamily: 'var(--font-mono)', fontSize: 20, fontWeight: 700, color: 'var(--copper)' }}>{prModal.weight}kg×{prModal.reps}</div>
                    <div style={{ fontSize: 11, color: 'var(--copper)', marginTop: 2, fontWeight: 600 }}>nuevo PR</div>
                  </div>
                </div>
                <button className="btn btn-primary btn-full" onClick={() => setPrModal(null)}>Continuar</button>
              </div>
            </div>
          </div>
        )}

        {/* Discard confirm */}
        {phase === 'cancel-confirm' && (
          <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 90, display: 'flex', alignItems: 'flex-end' }}>
            <div style={{ background: 'var(--surface)', borderRadius: '20px 20px 0 0', padding: '24px', width: '100%', paddingBottom: 40 }}>
              <div style={{ fontWeight: 700, fontSize: 18, marginBottom: 8 }}>¿Descartar sesión?</div>
              <div className="t-small t-secondary" style={{ marginBottom: 20 }}>Se perderán los {completedCount} series completadas. Esta acción no se puede deshacer.</div>
              <div style={{ display: 'flex', gap: 10 }}>
                <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setPhase('active')}>Volver</button>
                <button className="btn btn-danger" style={{ flex: 1 }} onClick={discardAndExit}>Descartar</button>
              </div>
            </div>
          </div>
        )}

        {/* Top bar */}
        <div style={{ background: 'var(--surface)', borderBottom: '1px solid var(--border)', padding: '12px 16px', flexShrink: 0 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <div>
              <div style={{ fontWeight: 700, fontSize: 17, letterSpacing: '-0.3px' }}>{activeSession.dayLabel}</div>
              <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginTop: 2 }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--text2)' }}>{elapsedDisplay}</span>
                <span style={{ fontSize: 12, color: 'var(--text2)' }}>{activeSession.routineName}</span>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: 13, color: 'var(--text2)', fontWeight: 600 }}>{completedCount}/{totalSets}</span>
              <button className="btn btn-ghost btn-sm btn-icon" style={{ padding: 8, color: 'var(--text3)' }} onClick={() => setPhase('cancel-confirm')}>
                <div style={{ width: 18, height: 18 }}><Icon.X /></div>
              </button>
              <button className="btn btn-primary btn-sm" onClick={() => setPhase('finishing')} style={{ whiteSpace: 'nowrap' }}>Finalizar</button>
            </div>
          </div>
          <div style={{ height: 3, background: 'var(--surface-alt)', borderRadius: 2, overflow: 'hidden' }}>
            <div style={{ height: '100%', background: 'var(--primary)', width: `${progress * 100}%`, transition: 'width 0.4s ease', borderRadius: 2 }} />
          </div>
        </div>

        {/* Exercise list */}
        <div style={{ flex: 1, overflowY: 'auto', paddingBottom: timerActive ? 140 : 100 }}>
          {exerciseGroups.map((exGroup, gi) => (
            <ExerciseBlock
              key={gi}
              exGroup={exGroup}
              onComplete={completeSet}
              onUpdate={updateSetField}
              bestSets={bestSets}
            />
          ))}

          {/* Session notes */}
          <div style={{ padding: '16px 16px 8px' }}>
            <div className="t-label t-secondary" style={{ marginBottom: 6 }}>Notas de sesión</div>
            <textarea
              className="input"
              style={{ minHeight: 68, resize: 'none', fontSize: 14, lineHeight: 1.5 }}
              placeholder="¿Algo que recordar de este entreno?"
              value={activeSession.notes}
              onChange={e => setActiveSession(s => ({ ...s, notes: e.target.value }))}
            />
          </div>
        </div>

        {/* Floating timer island */}
        {timerActive && (
          <div className="floating-island" style={{ cursor: 'pointer', bottom: 'calc(var(--nav-height) + 12px)' }} onClick={() => setTimerExpanded(v => !v)}>
            <TimerRadial
              seconds={timerSecs} total={timerTotal}
              isRunning={timerRunning}
              onToggle={(e) => { e && e.stopPropagation(); setTimerRunning(v => !v); }}
              onReset={() => { setTimerSecs(timerTotal); setTimerRunning(false); }}
              onClose={() => { setTimerSecs(0); setTimerRunning(false); }}
              compact={true}
            />
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.55)', marginBottom: 3, fontWeight: 500, letterSpacing: 0.5, textTransform: 'uppercase' }}>Descanso</div>
              <div style={{ display: 'flex', gap: 6 }}>
                {[60, 90, 120].map(d => (
                  <button key={d} onClick={e => { e.stopPropagation(); startTimer(d); }} style={{
                    padding: '3px 9px', borderRadius: 6, border: 'none',
                    background: d === timerTotal ? 'rgba(255,255,255,0.22)' : 'rgba(255,255,255,0.08)',
                    color: d === timerTotal ? 'rgba(255,255,255,1)' : 'rgba(255,255,255,0.55)',
                    fontFamily: 'var(--font-mono)', fontSize: 11, fontWeight: 600,
                    cursor: 'pointer', transition: 'var(--transition)',
                  }}>{d}s</button>
                ))}
              </div>
            </div>
            <button
              onClick={e => { e.stopPropagation(); setTimerRunning(v => !v); }}
              style={{ background: 'rgba(255,255,255,0.12)', border: 'none', color: 'white', cursor: 'pointer', padding: '8px', borderRadius: 8, flexShrink: 0 }}
            >
              <div style={{ width: 18, height: 18 }}>{timerRunning ? <Icon.Pause /> : <Icon.Play />}</div>
            </button>
          </div>
        )}

        {/* Timer starter chips when idle — only show after first set done */}
        {!timerActive && completedCount > 0 && (
          <div style={{
            position: 'fixed', bottom: 'calc(var(--nav-height) + 12px)', left: '50%', transform: 'translateX(-50%)',
            display: 'flex', gap: 6, zIndex: 40, alignItems: 'center',
            background: 'var(--surface)', border: '1px solid var(--border)',
            borderRadius: 'var(--radius-pill)', padding: '6px 14px',
            boxShadow: 'var(--shadow-md)',
          }}>
            <span style={{ fontSize: 11, color: 'var(--text3)', marginRight: 2, fontWeight: 500 }}>Descanso</span>
            {[60, 90, 120].map(d => (
              <button key={d} style={{
                padding: '5px 11px', borderRadius: 'var(--radius-pill)', border: 'none',
                background: d === timerTotal ? 'var(--primary)' : 'var(--surface-alt)',
                color: d === timerTotal ? '#fff' : 'var(--text2)',
                fontFamily: 'var(--font-mono)', fontSize: 11, fontWeight: 600, cursor: 'pointer',
                transition: 'var(--transition)',
              }} onClick={() => startTimer(d)}>{d}s</button>
            ))}
          </div>
        )}
      </div>
    );
  }

  // ── Finishing bottom sheet ────────────────────────────────────────────
  if (phase === 'finishing' && activeSession) {
    const completedSets = activeSession.sets.filter(s => s.completed);
    const volume = completedSets.reduce((a, s) => a + s.weight * s.reps, 0);
    const prs = completedSets.filter(s => s.isPR).length;
    const dur = Math.max(Math.floor(elapsedSecs / 60), 1);
    const incomplete = activeSession.sets.length - completedSets.length;

    return (
      <div style={{ position: 'relative', height: '100%' }}>
        {/* Blurred bg */}
        <div style={{ position: 'absolute', inset: 0, background: 'var(--bg)', opacity: 0.5 }} />
        <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'flex-end', zIndex: 80 }}>
          <div className="anim-fade-up" style={{ background: 'var(--surface)', borderRadius: '24px 24px 0 0', padding: '8px 24px 40px', width: '100%', boxShadow: 'var(--shadow-lg)' }}>
            <div style={{ width: 36, height: 4, background: 'var(--surface-alt2)', borderRadius: 2, margin: '12px auto 20px' }} />
            <div style={{ fontWeight: 700, fontSize: 21, letterSpacing: '-0.5px', marginBottom: 4 }}>¿Finalizar sesión?</div>
            {incomplete > 0 && (
              <div style={{ fontSize: 13, color: 'var(--text2)', marginBottom: 16, display: 'flex', alignItems: 'center', gap: 6 }}>
                <div style={{ width: 14, height: 14, color: 'var(--copper)' }}><Icon.Info /></div>
                {incomplete} serie{incomplete > 1 ? 's' : ''} sin completar se omitirán
              </div>
            )}

            {/* Summary metrics */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8, marginBottom: 16 }}>
              {[
                { v: `${dur}`, u: 'min', l: 'Duración' },
                { v: `${completedSets.length}`, u: '', l: 'Series' },
                { v: formatVolume(volume), u: 'kg', l: 'Volumen' },
              ].map(m => (
                <div key={m.l} className="metric-card" style={{ padding: '12px 14px' }}>
                  <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, fontSize: 20, color: 'var(--text)', letterSpacing: '-0.5px' }}>
                    {m.v}<span style={{ fontSize: 11, color: 'var(--text2)', marginLeft: 2 }}>{m.u}</span>
                  </div>
                  <div style={{ fontSize: 11, color: 'var(--text2)', marginTop: 2 }}>{m.l}</div>
                </div>
              ))}
            </div>

            {prs > 0 && (
              <div style={{ background: 'var(--copper-soft)', border: '1.5px solid var(--copper)', borderRadius: 10, padding: '10px 14px', marginBottom: 16, display: 'flex', alignItems: 'center', gap: 10 }}>
                <div style={{ width: 18, height: 18, color: 'var(--copper)', flexShrink: 0 }}><Icon.Zap /></div>
                <span style={{ color: 'var(--copper)', fontWeight: 600, fontSize: 14 }}>
                  {prs} nuevo{prs > 1 ? 's' : ''} PR{prs > 1 ? 's' : ''} en esta sesión
                </span>
              </div>
            )}

            {/* Notes preview */}
            {activeSession.notes ? (
              <div style={{ background: 'var(--bg)', borderRadius: 8, padding: '10px 12px', marginBottom: 16, fontSize: 13, color: 'var(--text2)', lineHeight: 1.5 }}>
                "{activeSession.notes}"
              </div>
            ) : null}

            <div style={{ display: 'flex', gap: 10 }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setPhase('active')}>
                Continuar
              </button>
              <button className="btn btn-primary" style={{ flex: 2 }} onClick={saveAndExit}>
                Guardar sesión
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ── Summary / post-workout ────────────────────────────────────────────
  if (phase === 'summary' && activeSession) {
    const saved = activeSession._saved;
    const prs = saved.sets.filter(s => s.isPR);
    const byExercise = Object.entries(
      saved.sets.reduce((acc, s) => {
        if (!acc[s.exerciseId]) acc[s.exerciseId] = [];
        acc[s.exerciseId].push(s);
        return acc;
      }, {})
    ).map(([exId, sets]) => ({
      exId,
      exDef: Object.values(M.exercises).find(e => e.id === exId),
      sets,
      bestSet: sets.reduce((b, s) => s.weight * s.reps > b.weight * b.reps ? s : b, sets[0]),
    }));

    return (
      <div className="screen anim-fade-up">
        {/* Header */}
        <div style={{ padding: '32px 20px 20px', textAlign: 'center' }}>
          <div style={{
            width: 56, height: 56, borderRadius: 18, background: 'var(--primary)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 16px',
          }}>
            <div style={{ width: 28, height: 28, color: '#fff' }}><Icon.Check /></div>
          </div>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.5px', marginBottom: 4 }}>Sesión guardada</div>
          <div className="t-small t-secondary">{saved.dayLabel} · {saved.routineName}</div>
        </div>

        <div style={{ padding: '0 16px' }}>
          {/* Stats */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, marginBottom: 16 }}>
            <Metric value={saved.durationMinutes} label="Duración" unit="min" />
            <Metric value={saved.totalSets} label="Series" />
            <Metric value={formatVolume(saved.volume)} label="Volumen" unit="kg" />
          </div>

          {/* PRs */}
          {prs.length > 0 && (
            <div style={{ marginBottom: 16 }}>
              <div className="section-label" style={{ padding: '0 0 8px' }}>Récords personales</div>
              {prs.map((set, i) => {
                const exDef = Object.values(M.exercises).find(e => e.id === set.exerciseId);
                return (
                  <div key={i} style={{
                    display: 'flex', alignItems: 'center', gap: 12,
                    padding: '10px 14px', background: 'var(--copper-soft)',
                    border: '1px solid var(--copper)', borderRadius: 10, marginBottom: 8,
                  }}>
                    <div style={{ width: 18, height: 18, color: 'var(--copper)' }}><Icon.Zap /></div>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--copper)' }}>{exDef?.name || set.exerciseId}</div>
                    </div>
                    <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, fontSize: 15, color: 'var(--copper)' }}>
                      {set.weight}kg×{set.reps}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {/* Exercise breakdown */}
          <div className="section-label" style={{ padding: '0 0 8px' }}>Resumen por ejercicio</div>
          <div className="card" style={{ marginBottom: 16, padding: 0, overflow: 'hidden' }}>
            {byExercise.map(({ exId, exDef, sets, bestSet }, i) => (
              <div key={exId} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '12px 16px',
                borderBottom: i < byExercise.length - 1 ? '1px solid var(--border)' : 'none',
              }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{exDef?.name || exId}</div>
                  <div style={{ fontSize: 11, color: 'var(--text3)', fontFamily: 'var(--font-mono)' }}>
                    {sets.length} series · vol {sets.reduce((a, s) => a + s.weight * s.reps, 0)}kg
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, fontSize: 14 }}>
                    {bestSet.weight}kg×{bestSet.reps}
                  </div>
                  {sets.some(s => s.isPR) && <span className="badge badge-pr" style={{ marginTop: 2 }}>PR</span>}
                </div>
              </div>
            ))}
          </div>

          {/* Notes */}
          {saved.notes && (
            <div style={{ marginBottom: 16 }}>
              <div className="section-label" style={{ padding: '0 0 8px' }}>Notas</div>
              <div className="card" style={{ fontSize: 14, color: 'var(--text2)', lineHeight: 1.6 }}>{saved.notes}</div>
            </div>
          )}

          <button className="btn btn-primary btn-full" style={{ marginBottom: 16, padding: '14px' }} onClick={() => setPhase('preview')}>
            Listo
          </button>
        </div>
      </div>
    );
  }

  return null;
}

function ExerciseBlock({ exGroup, onComplete, onUpdate, bestSets }) {
  const [collapsed, setCollapsed] = useState(false);
  const [noteSetId, setNoteSetId] = useState(null);
  const allDone = exGroup.sets.every(s => s.completed);
  const doneSets = exGroup.sets.filter(s => s.completed).length;
  const best = bestSets[exGroup.exerciseId];

  return (
    <div style={{ borderBottom: '1px solid var(--border)' }}>
      {/* Exercise header */}
      <div
        style={{
          display: 'flex', alignItems: 'center', gap: 12,
          padding: '14px 16px', cursor: 'pointer',
          background: allDone ? 'var(--primary-subtle)' : 'transparent',
          transition: 'background 0.25s ease',
        }}
        onClick={() => setCollapsed(v => !v)}
      >
        <div style={{
          width: 30, height: 30, borderRadius: 9, flexShrink: 0,
          background: allDone ? 'var(--primary)' : doneSets > 0 ? 'var(--primary-soft)' : 'var(--surface-alt)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          transition: 'background 0.25s ease',
        }}>
          {allDone
            ? <div style={{ width: 14, height: 14, color: '#fff' }}><Icon.Check /></div>
            : <div style={{ width: 14, height: 14, color: doneSets > 0 ? 'var(--primary)' : 'var(--text3)' }}><Icon.Dumbbell /></div>
          }
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontWeight: 600, fontSize: 15, color: allDone ? 'var(--primary)' : 'var(--text)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {exGroup.exDef?.name || exGroup.exerciseId}
          </div>
          <div style={{ fontSize: 11, color: 'var(--text3)', display: 'flex', gap: 8, alignItems: 'center', marginTop: 1 }}>
            <span>{exGroup.exDef?.muscleGroup}</span>
            {best && <span style={{ fontFamily: 'var(--font-mono)' }}>· PR {best.weight}kg×{best.reps}</span>}
          </div>
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexShrink: 0 }}>
          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: allDone ? 'var(--primary)' : 'var(--text3)', fontWeight: 600 }}>
            {doneSets}/{exGroup.sets.length}
          </span>
          <div style={{ width: 16, height: 16, color: 'var(--text3)', transform: collapsed ? 'rotate(-90deg)' : 'none', transition: '0.2s' }}>
            <Icon.ChevronDown />
          </div>
        </div>
      </div>

      {!collapsed && (
        <div style={{ padding: '0 16px 14px', background: 'var(--surface)' }}>
          {/* Column headers */}
          <div className="set-row" style={{ marginBottom: 6, paddingBottom: 4, borderBottom: '1px solid var(--border)' }}>
            <div style={{ fontSize: 9, color: 'var(--text3)', textAlign: 'center', fontWeight: 700, letterSpacing: 0.8, textTransform: 'uppercase' }}>#</div>
            <div style={{ fontSize: 9, color: 'var(--text3)', fontWeight: 700, letterSpacing: 0.8, textAlign: 'center', textTransform: 'uppercase' }}>KG</div>
            <div style={{ fontSize: 9, color: 'var(--text3)', fontWeight: 700, letterSpacing: 0.8, textAlign: 'center', textTransform: 'uppercase' }}>REPS</div>
            <div />
          </div>

          {exGroup.sets.map((set) => (
            <div key={set.id}>
              <div className="set-row" style={{
                opacity: set.completed ? 0.65 : 1,
                transition: 'opacity 0.2s',
                paddingTop: 5, paddingBottom: 5,
              }}>
                {/* Set number — tap to add note */}
                <button
                  style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                  onClick={() => setNoteSetId(noteSetId === set.id ? null : set.id)}
                  title="Añadir nota a esta serie"
                >
                  <div style={{
                    width: 24, height: 24, borderRadius: 6,
                    background: set.notes ? 'var(--primary-soft)' : 'transparent',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: set.notes ? 'var(--primary)' : 'var(--text3)', fontWeight: 600 }}>
                      {set.setNum}
                    </span>
                  </div>
                </button>

                {/* Weight */}
                <div className="num-input-group" style={{ height: 42 }}>
                  <button className="num-input-btn" onClick={() => onUpdate(set.id, 'weight', Math.max(0, parseFloat(set.weight) - 2.5))}>−</button>
                  <input
                    value={set.weight}
                    onChange={e => onUpdate(set.id, 'weight', parseFloat(e.target.value) || 0)}
                    style={{ width: 52 }}
                  />
                  <button className="num-input-btn" onClick={() => onUpdate(set.id, 'weight', parseFloat(set.weight) + 2.5)}>+</button>
                </div>

                {/* Reps */}
                <div className="num-input-group" style={{ height: 42 }}>
                  <button className="num-input-btn" onClick={() => onUpdate(set.id, 'reps', Math.max(1, parseInt(set.reps) - 1))}>−</button>
                  <input
                    value={set.reps}
                    onChange={e => onUpdate(set.id, 'reps', parseInt(e.target.value) || 1)}
                    style={{ width: 52 }}
                  />
                  <button className="num-input-btn" onClick={() => onUpdate(set.id, 'reps', parseInt(set.reps) + 1)}>+</button>
                </div>

                {/* Complete button */}
                <button
                  style={{
                    width: 34, height: 34, borderRadius: 9, flexShrink: 0,
                    border: `2px solid ${set.completed ? 'var(--primary)' : 'var(--border)'}`,
                    background: set.completed ? 'var(--primary)' : 'transparent',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    cursor: set.completed ? 'default' : 'pointer',
                    transition: 'all 0.18s cubic-bezier(0.34,1.56,0.64,1)',
                    position: 'relative',
                  }}
                  onClick={() => !set.completed && onComplete(set.id)}
                >
                  {set.completed
                    ? <div style={{ width: 15, height: 15, color: '#fff' }}><Icon.Check /></div>
                    : <div style={{ width: 12, height: 12, color: 'var(--text3)', opacity: 0.5 }}><Icon.Check /></div>
                  }
                  {set.isPR && set.completed && (
                    <div style={{ position: 'absolute', top: -7, right: -7 }}>
                      <span className="badge badge-pr" style={{ padding: '2px 5px', fontSize: 8, letterSpacing: 0.5 }}>PR</span>
                    </div>
                  )}
                </button>
              </div>

              {/* Inline note for this set */}
              {noteSetId === set.id && (
                <div style={{ paddingBottom: 6, paddingLeft: 32 }}>
                  <input
                    className="input"
                    style={{ fontSize: 13, padding: '7px 10px', borderRadius: 7 }}
                    placeholder="Nota para esta serie…"
                    value={set.notes}
                    onChange={e => onUpdate(set.id, 'notes', e.target.value)}
                    autoFocus
                  />
                </div>
              )}
            </div>
          ))}

          {/* Previous best hint */}
          {best && (
            <div style={{
              marginTop: 8, padding: '7px 10px',
              background: 'var(--bg)', borderRadius: 8,
              display: 'flex', alignItems: 'center', gap: 8,
            }}>
              <div style={{ width: 12, height: 12, color: 'var(--text3)' }}><Icon.TrendingUp /></div>
              <span style={{ fontSize: 11, color: 'var(--text3)' }}>
                Mejor marcada: <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--text2)' }}>{best.weight}kg × {best.reps} reps</span>
                <span style={{ marginLeft: 6, color: 'var(--text3)' }}>· 1RM ~{estimate1RM(best.weight, best.reps)}kg</span>
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: HISTORY
// ═══════════════════════════════════════════════════════════════════════════
function HistoryScreen({ sessions, onMenuOpen }) {
  const [selectedSession, setSelectedSession] = useState(null);
  const [period, setPeriod] = useState('all');
  const [sortBy, setSortBy] = useState('recent');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const t = setTimeout(() => setLoading(false), 600);
    return () => clearTimeout(t);
  }, []);

  const filtered = useMemo(() => {
    let list = [...sessions];
    if (period === '4w') {
      const cutoff = new Date('2026-05-03'); cutoff.setDate(cutoff.getDate() - 28);
      list = list.filter(s => new Date(s.date) >= cutoff);
    } else if (period === '12w') {
      const cutoff = new Date('2026-05-03'); cutoff.setDate(cutoff.getDate() - 84);
      list = list.filter(s => new Date(s.date) >= cutoff);
    }
    if (sortBy === 'oldest') list.sort((a, b) => new Date(a.date) - new Date(b.date));
    else if (sortBy === 'volume') list.sort((a, b) => b.volume - a.volume);
    else list.sort((a, b) => new Date(b.date) - new Date(a.date));
    return list;
  }, [sessions, period, sortBy]);

  if (selectedSession) {
    const idx = sessions.findIndex(s => s.id === selectedSession.id);
    const prevSession = sessions.slice(idx + 1).find(s => s.dayId === selectedSession.dayId);
    return <HistoryDetail session={selectedSession} prevSession={prevSession} onBack={() => setSelectedSession(null)} />;
  }

  // Group by week
  const grouped = filtered.reduce((acc, s) => {
    const week = `Semana ${getWeekNumber(s.date)} · ${new Date(s.date).getFullYear()}`;
    if (!acc[week]) acc[week] = [];
    acc[week].push(s);
    return acc;
  }, {});

  return (
    <div className="screen anim-fade-up">
      <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        {onMenuOpen && (
          <button className="btn btn-ghost btn-icon" onClick={onMenuOpen} style={{ color: 'var(--text2)', padding: 6 }}>
            <div style={{ width: 20, height: 20 }}><Icon.Menu /></div>
          </button>
        )}
        <div style={{ flex: 1 }}>
          <div className="page-header-title" style={{ marginBottom: 12 }}>Historial</div>
        {/* Filters */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
          {[{ v: 'all', l: 'Todo' }, { v: '12w', l: '12 sem' }, { v: '4w', l: '4 sem' }].map(opt => (
            <div key={opt.v} className={`chip ${period === opt.v ? 'chip-active' : 'chip-inactive'}`} onClick={() => setPeriod(opt.v)}>{opt.l}</div>
          ))}
          <div style={{ flex: 1 }} />
          <select
            className="input"
            style={{ width: 'auto', padding: '6px 10px', fontSize: 12, borderRadius: 8 }}
            value={sortBy}
            onChange={e => setSortBy(e.target.value)}
          >
            <option value="recent">Reciente</option>
            <option value="oldest">Antiguo</option>
            <option value="volume">Mayor volumen</option>
          </select>
        </div>
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '0 16px' }}>
          {Array.from({ length: 5 }).map((_, i) => <SkeletonCard key={i} rows={3} />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <div style={{ width: 48, height: 48, borderRadius: 14, background: 'var(--surface-alt)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ width: 28, height: 28, color: 'var(--text3)' }}><Icon.History /></div>
          </div>
          <div style={{ fontWeight: 600, color: 'var(--text)' }}>Sin sesiones en este período</div>
          <div className="t-small t-secondary">Cambia el filtro o completa un entrenamiento.</div>
        </div>
      ) : (
        <div style={{ padding: '0 16px 24px' }}>
          {Object.entries(grouped).map(([week, weekSessions]) => (
            <div key={week}>
              <div className="section-label" style={{ padding: '0 0 8px', marginTop: 16 }}>{week} · {weekSessions.length} sesión{weekSessions.length>1?'es':''}</div>
              {weekSessions.map(s => (
                <HistoryCard key={s.id} session={s} onClick={() => setSelectedSession(s)} />
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function HistoryCard({ session, onClick }) {
  const hasPR = session.sets.some(s => s.isPR);
  return (
    <div className="card" style={{ marginBottom: 8, cursor: 'pointer' }} onClick={onClick}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
        <div>
          <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 2 }}>{session.dayLabel}</div>
          <div className="t-tiny t-secondary">{session.routineName} · {formatDate(session.date)}</div>
        </div>
        <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
          {hasPR && <span className="badge badge-copper">PR</span>}
          <span className="badge badge-neutral">{session.durationMinutes} min</span>
        </div>
      </div>
      <div style={{ display: 'flex', gap: 20 }}>
        <div>
          <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, fontSize: 16, letterSpacing: '-0.5px' }}>{formatVolume(session.volume)}</span>
          <span style={{ fontSize: 11, color: 'var(--text3)', marginLeft: 2, fontFamily: 'var(--font-mono)' }}>kg</span>
          <div className="t-tiny t-secondary">Volumen</div>
        </div>
        <div>
          <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, fontSize: 16, letterSpacing: '-0.5px' }}>{session.totalSets}</span>
          <div className="t-tiny t-secondary">Series</div>
        </div>
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
          <div style={{ width: 16, height: 16, color: 'var(--text3)' }}><Icon.ChevronRight /></div>
        </div>
      </div>
    </div>
  );
}

function HistoryDetail({ session, prevSession, onBack }) {
  const exGroups = Object.entries(
    session.sets.reduce((acc, s) => {
      if (!acc[s.exerciseId]) acc[s.exerciseId] = [];
      acc[s.exerciseId].push(s);
      return acc;
    }, {})
  ).map(([exId, sets]) => ({
    exId, sets,
    exDef: Object.values(M.exercises).find(e => e.id === exId),
    volume: sets.reduce((a, s) => a + s.weight * s.reps, 0),
    bestSet: sets.reduce((best, s) => s.weight * s.reps > (best.weight * best.reps) ? s : best, sets[0]),
  }));

  const overallBest = exGroups.reduce((b, g) => g.bestSet.weight * g.bestSet.reps > (b?.weight * b?.reps || 0) ? g.bestSet : b, null);

  function Delta({ current, prev, label, formatter = v => v }) {
    if (prev == null) return (
      <div className="metric-card">
        <div className="metric-value" style={{ fontSize: 18 }}>{formatter(current)}</div>
        <div className="metric-label">{label}</div>
        <div className="t-tiny" style={{ color: 'var(--text3)' }}>Primera sesión comparable</div>
      </div>
    );
    const diff = current - prev;
    const pct = prev !== 0 ? Math.round((diff / prev) * 100) : 0;
    return (
      <div className="metric-card">
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
          <div className="metric-value" style={{ fontSize: 18 }}>{formatter(current)}</div>
          <div style={{
            fontSize: 12, fontFamily: 'var(--font-mono)', fontWeight: 600,
            color: diff > 0 ? 'var(--primary)' : diff < 0 ? 'var(--error)' : 'var(--text3)',
          }}>
            {diff > 0 ? <span>↑ +{pct}%</span> : diff < 0 ? <span>↓ {pct}%</span> : <span>— 0%</span>}
          </div>
        </div>
        <div className="metric-label">{label}</div>
      </div>
    );
  }

  return (
    <div className="screen anim-fade-up">
      <div style={{ padding: '16px 16px 8px', display: 'flex', alignItems: 'center', gap: 10, position: 'sticky', top: 0, background: 'var(--bg)', zIndex: 10 }}>
        <button className="btn btn-ghost btn-icon" onClick={onBack}><div style={{ width: 20, height: 20 }}><Icon.ChevronLeft /></div></button>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 700, fontSize: 17 }}>{session.dayLabel}</div>
          <div className="t-tiny t-secondary">{formatDate(session.date)} · {session.routineName}</div>
        </div>
        <span className="badge badge-neutral">{session.durationMinutes} min</span>
      </div>

      <div style={{ padding: '0 16px' }}>
        {/* Comparativa */}
        <div className="section-label" style={{ padding: '0 0 8px', marginTop: 8 }}>
          {prevSession ? `vs. ${formatDateShort(prevSession.date)}` : 'Métricas'}
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 16 }}>
          <Delta current={session.volume} prev={prevSession?.volume} label="Volumen (kg)" formatter={v => formatVolume(v)} />
          <Delta current={session.durationMinutes} prev={prevSession?.durationMinutes} label="Duración (min)" />
          <Delta current={session.totalSets} prev={prevSession?.totalSets} label="Series" />
          <Delta current={overallBest ? overallBest.weight : 0} prev={prevSession ? prevSession.sets.reduce((b, s) => s.weight > b ? s.weight : b, 0) : null} label="Peso máximo (kg)" />
        </div>

        {/* Exercises */}
        <div className="section-label" style={{ padding: '0 0 8px' }}>Ejercicios</div>
        {exGroups.map(({ exId, sets, exDef, bestSet }) => (
          <div key={exId} className="card" style={{ marginBottom: 10 }}>
            <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 8 }}>{exDef?.name || exId}</div>
            {sets.map((set, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '6px 0', borderBottom: i < sets.length - 1 ? '1px solid var(--border)' : 'none',
              }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--text3)' }}>Serie {i + 1}</span>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, fontSize: 14 }}>{set.weight}kg × {set.reps}</span>
                  {set.isPR && <span className="badge badge-pr">PR</span>}
                </div>
              </div>
            ))}
            <div style={{ marginTop: 8, padding: '6px 10px', background: 'var(--bg)', borderRadius: 6 }}>
              <span className="t-tiny t-secondary">Mejor: </span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, fontWeight: 600 }}>{bestSet.weight}kg × {bestSet.reps} = {bestSet.weight * bestSet.reps}kg vol · 1RM ~{estimate1RM(bestSet.weight, bestSet.reps)}kg</span>
            </div>
          </div>
        ))}

        {/* Session notes */}
        {session.notes && (
          <div style={{ marginBottom: 16 }}>
            <div className="section-label" style={{ padding: '0 0 8px' }}>Notas</div>
            <div className="card" style={{ fontSize: 14, color: 'var(--text2)', lineHeight: 1.6 }}>{session.notes}</div>
          </div>
        )}
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: STATS
// ═══════════════════════════════════════════════════════════════════════════
function StatsScreen({ sessions, onMenuOpen }) {
  const [period, setPeriod] = useState('all');
  const [selectedEx, setSelectedEx] = useState('bench_press');
  const [tooltip, setTooltip] = useState(null);
  const [heatmapView, setHeatmapView] = useState(true);
  const chartRef = useRef(null);

  const filtered = useMemo(() => {
    let list = [...sessions];
    if (period === '4w') {
      const cutoff = new Date('2026-05-03'); cutoff.setDate(cutoff.getDate() - 28);
      list = list.filter(s => new Date(s.date) >= cutoff);
    } else if (period === '12w') {
      const cutoff = new Date('2026-05-03'); cutoff.setDate(cutoff.getDate() - 84);
      list = list.filter(s => new Date(s.date) >= cutoff);
    }
    return list.sort((a, b) => new Date(a.date) - new Date(b.date));
  }, [sessions, period]);

  const exStats = useMemo(() => M.getExerciseStats(selectedEx).filter(pt => {
    if (period === 'all') return true;
    const cutoff = new Date('2026-05-03');
    cutoff.setDate(cutoff.getDate() - (period === '4w' ? 28 : 84));
    return new Date(pt.date) >= cutoff;
  }), [selectedEx, period]);

  const bestSets = M.getBestSets();
  const heatmapData = M.getHeatmapData();

  const totalVolume = filtered.reduce((a, s) => a + s.volume, 0);
  const totalSessions = filtered.length;
  const avgDuration = filtered.length > 0 ? Math.round(filtered.reduce((a, s) => a + s.durationMinutes, 0) / filtered.length) : 0;

  // Volume chart data
  const volData = filtered.map(s => ({ date: s.date.slice(0, 10), volume: s.volume, label: s.dayLabel }));

  // Chart rendering
  function renderChart() {
    if (exStats.length < 2) return null;
    const W = 320, H = 140, PL = 40, PR = 16, PT = 16, PB = 24;
    const chartW = W - PL - PR, chartH = H - PT - PB;
    const vals = exStats.map(p => p.maxWeight);
    const minV = Math.min(...vals) * 0.95;
    const maxV = Math.max(...vals) * 1.05;
    const pts = exStats.map((p, i) => {
      const x = PL + (i / (exStats.length - 1)) * chartW;
      const y = PT + chartH - ((p.maxWeight - minV) / (maxV - minV)) * chartH;
      return { x, y, ...p };
    });
    const path = pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
    const area = `${path} L ${pts[pts.length-1].x} ${H - PB} L ${pts[0].x} ${H - PB} Z`;

    return (
      <div ref={chartRef} style={{ position: 'relative', overflow: 'visible' }}>
        <svg width="100%" viewBox={`0 0 ${W} ${H}`} style={{ overflow: 'visible' }}>
          {/* Grid lines */}
          {[0, 0.5, 1].map(t => {
            const y = PT + chartH - t * chartH;
            const val = Math.round(minV + t * (maxV - minV));
            return (
              <g key={t}>
                <line x1={PL} y1={y} x2={W - PR} y2={y} stroke="var(--border)" strokeWidth="1" strokeDasharray="3 3" />
                <text x={PL - 4} y={y + 4} textAnchor="end" fontSize="9" fill="var(--text3)" fontFamily="'JetBrains Mono'">{val}</text>
              </g>
            );
          })}
          {/* Area fill */}
          <path d={area} fill="var(--primary)" opacity="0.08" />
          {/* Line */}
          <path d={path} fill="none" stroke="var(--primary)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          {/* Points */}
          {pts.map((p, i) => (
            <circle
              key={i} cx={p.x} cy={p.y} r={tooltip?.idx === i ? 5 : 3.5}
              fill={tooltip?.idx === i ? 'var(--primary-dark)' : 'var(--primary)'}
              stroke="var(--surface)" strokeWidth="2"
              style={{ cursor: 'pointer', transition: 'r 0.15s ease' }}
              onMouseEnter={e => setTooltip({ idx: i, x: p.x, y: p.y, data: p })}
              onMouseLeave={() => setTooltip(null)}
              onTouchStart={e => { e.preventDefault(); setTooltip(tooltip?.idx === i ? null : { idx: i, x: p.x, y: p.y, data: p }); }}
            />
          ))}
        </svg>
        {tooltip && (
          <div className="chart-tooltip" style={{
            left: `${(tooltip.x / W) * 100}%`,
            top: `${(tooltip.y / 140) * 100}%`,
          }}>
            <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, fontSize: 13 }}>{tooltip.data.maxWeight}kg</div>
            <div style={{ fontSize: 10, opacity: 0.75 }}>{formatDateShort(tooltip.data.date)} · {tooltip.data.maxReps} reps</div>
            <div style={{ fontSize: 10, opacity: 0.75 }}>1RM ~{tooltip.data.estimated1RM}kg</div>
          </div>
        )}
      </div>
    );
  }

  // Heatmap — last 26 weeks (6 months) with proper labels
  function renderHeatmap() {
    const today = new Date('2026-05-18');
    // Start: 26 weeks ago, snapped to Monday
    const totalWeeks = 26;
    const totalDays = totalWeeks * 7;
    // Find this week's Monday
    const todayDow = today.getDay(); // 0=Sun
    const mondayOffset = todayDow === 0 ? -6 : 1 - todayDow;
    const thisMonday = new Date(today);
    thisMonday.setDate(today.getDate() + mondayOffset);
    const startDate = new Date(thisMonday);
    startDate.setDate(thisMonday.getDate() - (totalWeeks - 1) * 7);

    // Build grid: array of weeks, each week is 7 days (Mon-Sun)
    const weeks = [];
    for (let w = 0; w < totalWeeks; w++) {
      const week = [];
      for (let d = 0; d < 7; d++) {
        const date = new Date(startDate);
        date.setDate(startDate.getDate() + w * 7 + d);
        const key = date.toISOString().slice(0, 10);
        const count = heatmapData[key] || 0;
        const isFuture = date > today;
        week.push({ date, key, count, isFuture, monthIdx: date.getMonth() });
      }
      weeks.push(week);
    }

    // Stats
    const trainingDays = Object.keys(heatmapData).filter(k => new Date(k) >= startDate).length;
    // Current streak (consecutive days with sessions or rest, walking backwards from today)
    let currentStreak = 0;
    for (let i = 0; i < 60; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() - i);
      const k = d.toISOString().slice(0, 10);
      if (heatmapData[k]) currentStreak++;
      else if (i > 0) break;
      else continue;
    }
    // Longest streak in window
    let longestStreak = 0, run = 0;
    for (let i = 0; i < totalDays; i++) {
      const d = new Date(startDate);
      d.setDate(startDate.getDate() + i);
      const k = d.toISOString().slice(0, 10);
      if (heatmapData[k]) { run++; if (run > longestStreak) longestStreak = run; }
      else run = 0;
    }
    // Best week (max sessions in any 7-day window)
    const bestWeek = Math.max(...weeks.map(w => w.reduce((a, d) => a + d.count, 0)));

    // Month labels: detect when month changes between weeks (use first day of week)
    const monthNames = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
    const monthLabels = weeks.map((w, idx) => {
      const firstDay = w[0];
      // show label if it's the first week OR if month changed from previous week
      if (idx === 0) return monthNames[firstDay.monthIdx];
      const prevFirstDay = weeks[idx - 1][0];
      if (firstDay.monthIdx !== prevFirstDay.monthIdx) return monthNames[firstDay.monthIdx];
      return '';
    });

    const cellSize = 11;
    const cellGap = 3;
    const dayLabels = ['L','M','X','J','V','S','D'];

    function cellColor(count, isFuture) {
      if (isFuture) return 'transparent';
      if (count === 0) return 'var(--surface-alt)';
      // 3 levels of intensity
      if (count >= 2) return 'var(--primary)';
      return 'var(--primary-soft)';
    }

    return (
      <div>
        {/* Summary stats row */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8, marginBottom: 14 }}>
          <div style={{ padding: '8px 10px', background: 'var(--bg)', borderRadius: 8 }}>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: 18, fontWeight: 700, color: 'var(--text)' }}>{currentStreak}</div>
            <div style={{ fontSize: 10, color: 'var(--text2)', fontWeight: 500 }}>Racha actual</div>
          </div>
          <div style={{ padding: '8px 10px', background: 'var(--bg)', borderRadius: 8 }}>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: 18, fontWeight: 700, color: 'var(--text)' }}>{longestStreak}</div>
            <div style={{ fontSize: 10, color: 'var(--text2)', fontWeight: 500 }}>Mejor racha</div>
          </div>
          <div style={{ padding: '8px 10px', background: 'var(--bg)', borderRadius: 8 }}>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: 18, fontWeight: 700, color: 'var(--text)' }}>{trainingDays}</div>
            <div style={{ fontSize: 10, color: 'var(--text2)', fontWeight: 500 }}>Días entrenados</div>
          </div>
        </div>

        {/* Heatmap grid */}
        <div style={{ overflowX: 'auto', overflowY: 'hidden', paddingBottom: 4, scrollbarWidth: 'none' }}>
          <div style={{ display: 'inline-flex', flexDirection: 'column', gap: 4 }}>
            {/* Month labels row */}
            <div style={{ display: 'flex', marginLeft: 18, gap: cellGap }}>
              {monthLabels.map((label, i) => (
                <div key={i} style={{
                  width: cellSize, fontSize: 9, color: 'var(--text3)',
                  fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.5,
                  whiteSpace: 'nowrap', position: 'relative',
                }}>
                  {label && <span style={{ position: 'absolute', left: 0 }}>{label}</span>}
                </div>
              ))}
            </div>

            {/* Days grid */}
            <div style={{ display: 'flex', gap: 6 }}>
              {/* Day labels column */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: cellGap, paddingTop: 0 }}>
                {dayLabels.map((d, i) => (
                  <div key={i} style={{
                    height: cellSize, width: 12,
                    fontSize: 9, color: 'var(--text3)', fontWeight: 600,
                    display: 'flex', alignItems: 'center',
                    visibility: i % 2 === 0 ? 'visible' : 'hidden',
                  }}>{d}</div>
                ))}
              </div>

              {/* Weeks */}
              <div style={{ display: 'flex', gap: cellGap }}>
                {weeks.map((week, wi) => (
                  <div key={wi} style={{ display: 'flex', flexDirection: 'column', gap: cellGap }}>
                    {week.map((day, di) => (
                      <div
                        key={di}
                        title={day.isFuture ? '' : `${day.key}: ${day.count} ${day.count === 1 ? 'sesión' : 'sesiones'}`}
                        style={{
                          width: cellSize, height: cellSize,
                          borderRadius: 2,
                          background: cellColor(day.count, day.isFuture),
                          border: day.key === today.toISOString().slice(0, 10) ? '1.5px solid var(--copper)' : 'none',
                          boxSizing: 'border-box',
                          cursor: day.isFuture ? 'default' : 'pointer',
                          transition: 'transform 0.1s ease',
                        }}
                      />
                    ))}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Legend */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 12, justifyContent: 'flex-end' }}>
          <span className="t-tiny t-secondary">Menos</span>
          <div className="heatmap-cell" style={{ background: 'var(--surface-alt)', width: 11, height: 11, borderRadius: 2 }} />
          <div className="heatmap-cell" style={{ background: 'var(--primary-soft)', width: 11, height: 11, borderRadius: 2 }} />
          <div className="heatmap-cell" style={{ background: 'var(--primary)', width: 11, height: 11, borderRadius: 2 }} />
          <span className="t-tiny t-secondary">Más</span>
        </div>
      </div>
    );
  }

  const trackableExercises = ['bench_press', 'squat', 'deadlift', 'ohp', 'bent_row', 'rdl'];

  return (
    <div className="screen anim-fade-up">
      <div className="page-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12, gap: 8 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            {onMenuOpen && (
              <button className="btn btn-ghost btn-icon" onClick={onMenuOpen} style={{ color: 'var(--text2)', padding: 6 }}>
                <div style={{ width: 20, height: 20 }}><Icon.Menu /></div>
              </button>
            )}
            <div className="page-header-title">Datos</div>
          </div>
          <div className="segment-control" style={{ width: 'auto' }}>
            {[{ v: 'all', l: 'Todo' }, { v: '12w', l: '12s' }, { v: '4w', l: '4s' }].map(opt => (
              <button key={opt.v} className={`segment-btn ${period === opt.v ? 'active' : ''}`} onClick={() => setPeriod(opt.v)}>{opt.l}</button>
            ))}
          </div>
        </div>
      </div>

      <div style={{ padding: '0 16px' }}>
        {/* Summary row */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8, marginBottom: 16 }}>
          <Metric value={totalSessions} label="Sesiones" />
          <Metric value={formatVolume(totalVolume)} label="Volumen total" unit="kg" />
          <Metric value={avgDuration} label="Prom. duración" unit="min" />
        </div>

        {/* Progress chart */}
        <div className="section-label" style={{ padding: '0 0 8px' }}>Progreso de fuerza</div>
        <div className="card" style={{ marginBottom: 16 }}>
          {/* Exercise selector */}
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 12 }}>
            {trackableExercises.map(exId => {
              const ex = M.exercises[exId];
              const shortName = ex?.name.split(' ').slice(0, 2).join(' ') || exId;
              return (
                <div key={exId} className={`chip ${selectedEx === exId ? 'chip-active' : 'chip-inactive'}`} style={{ fontSize: 11, padding: '4px 10px', whiteSpace: 'nowrap' }} onClick={() => setSelectedEx(exId)}>
                  {shortName}
                </div>
              );
            })}
          </div>

          {exStats.length >= 2 ? renderChart() : (
            <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text3)', fontSize: 13 }}>
              Sin datos suficientes para mostrar gráfica
            </div>
          )}
        </div>

        {/* Best marks */}
        <div className="section-label" style={{ padding: '0 0 8px' }}>Mejores marcas</div>
        <div className="card" style={{ marginBottom: 16 }}>
          {Object.entries(bestSets).slice(0, 8).map(([exId, set], i) => {
            const ex = M.exercises[exId];
            return (
              <div key={exId} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '10px 0', borderBottom: i < 7 ? '1px solid var(--border)' : 'none',
              }}>
                <div>
                  <div style={{ fontWeight: 500, fontSize: 14 }}>{ex?.name || exId}</div>
                  <div style={{ fontSize: 11, color: 'var(--text3)', fontFamily: 'var(--font-mono)' }}>{formatDateShort(set.date)}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, fontSize: 15 }}>{set.weight}kg × {set.reps}</div>
                  <div style={{ fontSize: 11, color: 'var(--text3)', fontFamily: 'var(--font-mono)' }}>1RM ~{estimate1RM(set.weight, set.reps)}kg</div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Heatmap */}
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 10 }}>
          <div className="section-label" style={{ padding: 0 }}>Actividad reciente</div>
          <span className="t-tiny t-secondary">Últimas 26 semanas</span>
        </div>
        <div className="card" style={{ marginBottom: 24, padding: '16px 16px 14px' }}>
          {renderHeatmap()}
        </div>
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: SETTINGS
// ═══════════════════════════════════════════════════════════════════════════
function SettingsScreen({ theme, setTheme, weightUnit, setWeightUnit, showToast, onBack }) {
  const [reloading, setReloading] = useState(false);

  function handleTheme(t) {
    setTheme(t);
    showToast(`Tema: ${t === 'system' ? 'Sistema' : t === 'light' ? 'Claro' : 'Oscuro'}`);
  }

  function handleUnit(u) {
    setWeightUnit(u);
    showToast(`Unidad: ${u.toUpperCase()}`);
  }

  function handleReload() {
    setReloading(true);
    setTimeout(() => { setReloading(false); showToast('Datos demo recargados'); }, 800);
  }

  return (
    <div className="screen anim-fade-up">
      <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        {onBack && <button className="btn btn-ghost btn-icon" onClick={onBack}><div style={{ width: 20, height: 20 }}><Icon.ChevronLeft /></div></button>}
        <div className="page-header-title">Ajustes</div>
      </div>

      <div style={{ padding: '0 16px' }}>
        {/* Tema */}
        <div className="section-label" style={{ padding: '0 0 8px', marginTop: 8 }}>Apariencia</div>
        <div className="card" style={{ padding: '16px', marginBottom: 16 }}>
          <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Tema</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
            {[
              { v: 'system', label: 'Sistema', Icon: Icon.Monitor },
              { v: 'light', label: 'Claro', Icon: Icon.Sun },
              { v: 'dark', label: 'Oscuro', Icon: Icon.Moon },
            ].map(({ v, label, Icon: Ic }) => (
              <button
                key={v}
                onClick={() => handleTheme(v)}
                style={{
                  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
                  padding: '12px 8px', borderRadius: 10,
                  border: `2px solid ${theme === v ? 'var(--primary)' : 'var(--border)'}`,
                  background: theme === v ? 'var(--primary-subtle)' : 'var(--surface-alt)',
                  cursor: 'pointer', transition: 'var(--transition)',
                }}
              >
                <div style={{ width: 22, height: 22, color: theme === v ? 'var(--primary)' : 'var(--text2)' }}><Ic /></div>
                <span style={{ fontSize: 12, fontWeight: theme === v ? 700 : 500, color: theme === v ? 'var(--primary)' : 'var(--text2)' }}>{label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Unidad peso */}
        <div className="section-label" style={{ padding: '0 0 8px' }}>Registro</div>
        <div className="card" style={{ padding: '16px', marginBottom: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 2 }}>Unidad de peso</div>
              <div className="t-tiny t-secondary">Afecta todos los valores registrados</div>
            </div>
            <div className="segment-control">
              <button className={`segment-btn ${weightUnit === 'kg' ? 'active' : ''}`} onClick={() => handleUnit('kg')}>kg</button>
              <button className={`segment-btn ${weightUnit === 'lb' ? 'active' : ''}`} onClick={() => handleUnit('lb')}>lb</button>
            </div>
          </div>
        </div>

        {/* Datos demo */}
        <div className="section-label" style={{ padding: '0 0 8px' }}>Datos</div>
        <div className="card" style={{ marginBottom: 24 }}>
          <div className="settings-row" style={{ padding: '14px 0' }} onClick={handleReload}>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600, fontSize: 14 }}>Recargar datos demo</div>
              <div className="t-tiny t-secondary">Restaura los datos de ejemplo originales</div>
            </div>
            {reloading ? (
              <div style={{ width: 18, height: 18, color: 'var(--text3)' }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                  <path d="M21 12a9 9 0 11-6.219-8.56" style={{ transformOrigin: 'center', animation: 'spin 0.8s linear infinite' }}/>
                </svg>
              </div>
            ) : (
              <div style={{ width: 18, height: 18, color: 'var(--text3)' }}><Icon.RotateCcw /></div>
            )}
          </div>
        </div>

        {/* Version */}
        <div style={{ textAlign: 'center', paddingBottom: 32 }}>
          <div style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--text3)' }}>FitTrackPlus {M.appVersion}</div>
          <div className="t-tiny t-secondary" style={{ marginTop: 4 }}>Local-first. Sin cuenta. Sin cloud.</div>
        </div>
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN: WIDGET PREVIEW
// ═══════════════════════════════════════════════════════════════════════════
function WidgetScreen({ sessions, onNavigate }) {
  const sessionsThisWeek = M.getSessionsThisWeek();
  const streak = 3; // mock streak

  return (
    <div className="screen anim-fade-up">
      <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <button className="btn btn-ghost btn-icon" onClick={() => onNavigate('home')}><div style={{ width: 20, height: 20 }}><Icon.ChevronLeft /></div></button>
        <div className="page-header-title" style={{ fontSize: 22 }}>Widget & Atajos</div>
      </div>

      <div style={{ padding: '0 16px' }}>
        <div className="section-label" style={{ padding: '0 0 8px', marginTop: 8 }}>Widgets Android</div>

        {/* 2×1 Widget */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 12, color: 'var(--text3)', marginBottom: 8, fontWeight: 500 }}>2×1 — Racha semanal</div>
          <div style={{
            background: 'var(--primary)',
            borderRadius: 16,
            padding: '14px 18px',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            width: '100%', maxWidth: 280,
            boxShadow: 'var(--shadow-md)',
            position: 'relative', overflow: 'hidden',
          }}>
            <div style={{ position: 'absolute', top: -16, right: -16, width: 80, height: 80, borderRadius: '50%', background: 'rgba(255,255,255,0.07)' }} />
            <div>
              <div style={{ fontSize: 10, fontWeight: 600, letterSpacing: 1, textTransform: 'uppercase', color: 'rgba(255,255,255,0.6)', marginBottom: 4 }}>Esta semana</div>
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: 28, fontWeight: 700, color: '#fff', lineHeight: 1 }}>{sessionsThisWeek}<span style={{ fontSize: 14, marginLeft: 2, opacity: 0.7 }}>/5</span></div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: 22, fontWeight: 700, color: '#fff' }}>{streak}</div>
              <div style={{ fontSize: 10, color: 'rgba(255,255,255,0.65)', fontWeight: 500 }}>días racha</div>
            </div>
          </div>
        </div>

        {/* 1×1 Widgets */}
        <div style={{ fontSize: 12, color: 'var(--text3)', marginBottom: 8, fontWeight: 500 }}>1×1 — Acción rápida</div>
        <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
          {[
            { label: 'Entrenar', icon: 'Play', tab: 'workout', bg: 'var(--primary)' },
            { label: 'Datos', icon: 'BarChart', tab: 'stats', bg: 'var(--surface)' },
          ].map(w => {
            const WIcon = Icon[w.icon];
            return (
              <div key={w.tab} style={{
                width: 110, height: 110,
                background: w.bg,
                borderRadius: 20,
                border: w.bg === 'var(--surface)' ? '1px solid var(--border)' : 'none',
                display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                gap: 10, cursor: 'pointer', boxShadow: 'var(--shadow-md)',
              }} onClick={() => onNavigate(w.tab)}>
                <div style={{ width: 32, height: 32, color: w.bg === 'var(--surface)' ? 'var(--primary)' : '#fff' }}><WIcon /></div>
                <div style={{ fontSize: 12, fontWeight: 600, color: w.bg === 'var(--surface)' ? 'var(--text)' : '#fff' }}>{w.label}</div>
              </div>
            );
          })}
        </div>

        {/* App shortcuts */}
        <div className="section-label" style={{ padding: '0 0 8px' }}>Atajos de aplicación</div>
        <div className="card" style={{ marginBottom: 24 }}>
          {[
            { label: 'Comenzar entrenamiento', sub: 'Abre Entrenar directamente', tab: 'workout', icon: 'Play' },
            { label: 'Ver estadísticas', sub: 'Acceso rápido a Datos', tab: 'stats', icon: 'BarChart' },
            { label: 'Última sesión', sub: 'Ir al historial reciente', tab: 'history', icon: 'History' },
          ].map((s, i) => {
            const SIcon = Icon[s.icon];
            return (
              <div key={s.tab} className="settings-row" style={{ padding: '12px 0', cursor: 'pointer' }} onClick={() => onNavigate(s.tab)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div style={{ width: 36, height: 36, borderRadius: 10, background: 'var(--primary-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <div style={{ width: 18, height: 18, color: 'var(--primary)' }}><SIcon /></div>
                  </div>
                  <div>
                    <div style={{ fontWeight: 500, fontSize: 14 }}>{s.label}</div>
                    <div className="t-tiny t-secondary">{s.sub}</div>
                  </div>
                </div>
                <div style={{ width: 16, height: 16, color: 'var(--text3)' }}><Icon.ChevronRight /></div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// APP ROOT
// ═══════════════════════════════════════════════════════════════════════════
function App() {
  const [appPhase, setAppPhase] = useState(() => localStorage.getItem('ft_onboarded') === '1' ? 'app' : 'launch');
  const [activeTab, setActiveTab] = useState('home');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [theme, setTheme] = useState(() => localStorage.getItem('ft_theme') || 'light');
  const [weightUnit, setWeightUnit] = useState(() => localStorage.getItem('ft_unit') || 'kg');
  const [routines, setRoutines] = useState(M.routines);
  const [sessions, setSessions] = useState(M.sessions);
  const [toastMsg, setToastMsg] = useState('');
  const [toastVisible, setToastVisible] = useState(false);
  const toastTimer = useRef(null);

  // Apply theme
  useEffect(() => {
    const el = document.documentElement;
    if (theme === 'dark') { el.setAttribute('data-theme', 'dark'); }
    else if (theme === 'light') { el.removeAttribute('data-theme'); }
    else {
      const mq = window.matchMedia('(prefers-color-scheme: dark)');
      if (mq.matches) el.setAttribute('data-theme', 'dark');
      else el.removeAttribute('data-theme');
    }
    localStorage.setItem('ft_theme', theme);
  }, [theme]);

  useEffect(() => { localStorage.setItem('ft_unit', weightUnit); }, [weightUnit]);

  function showToast(msg) {
    clearTimeout(toastTimer.current);
    setToastMsg(msg);
    setToastVisible(true);
    toastTimer.current = setTimeout(() => setToastVisible(false), 2200);
  }

  function handleSessionComplete(session) {
    setSessions(s => [session, ...s]);
  }

  const isInApp = appPhase === 'app';

  return (
    <div className="ft-app" data-theme-host="">
      {appPhase === 'launch' && <LaunchScreen onDone={() => setAppPhase('onboarding')} />}
      {appPhase === 'onboarding' && <OnboardingScreen onDone={() => { localStorage.setItem('ft_onboarded','1'); setAppPhase('app'); }} />}

      {isInApp && (
        <>
          {activeTab === 'home' && <HomeScreen onNavigate={setActiveTab} routines={routines} sessions={sessions} loading={false} onMenuOpen={() => setDrawerOpen(true)} />}
          {activeTab === 'routines' && <RoutinesScreen routines={routines} onRoutinesChange={setRoutines} showToast={showToast} onMenuOpen={() => setDrawerOpen(true)} />}
          {activeTab === 'workout' && (
            <div className="screen-full">
              <WorkoutScreen routines={routines} sessions={sessions} onSessionComplete={handleSessionComplete} showToast={showToast} onMenuOpen={() => setDrawerOpen(true)} />
            </div>
          )}
          {activeTab === 'history' && <HistoryScreen sessions={sessions} onMenuOpen={() => setDrawerOpen(true)} />}
          {activeTab === 'stats' && <StatsScreen sessions={sessions} onMenuOpen={() => setDrawerOpen(true)} />}
          {activeTab === 'settings' && <SettingsScreen theme={theme} setTheme={setTheme} weightUnit={weightUnit} setWeightUnit={setWeightUnit} showToast={showToast} onBack={() => setActiveTab('home')} />}
          {activeTab === 'widget' && <WidgetScreen sessions={sessions} onNavigate={setActiveTab} />}

          <BottomNav active={activeTab} onNavigate={setActiveTab} hasActiveRoutine={!!routines.find(r => r.isActive)} />
          <SideDrawer
            open={drawerOpen}
            onClose={() => setDrawerOpen(false)}
            onNavigate={setActiveTab}
            theme={theme} onThemeChange={setTheme}
            weightUnit={weightUnit} onUnitChange={setWeightUnit}
            showToast={showToast}
          />
          <Toast message={toastMsg} visible={toastVisible} />
        </>
      )}
    </div>
  );
}

const rootEl = document.getElementById('root');
const root = ReactDOM.createRoot(rootEl);
root.render(React.createElement(App));
