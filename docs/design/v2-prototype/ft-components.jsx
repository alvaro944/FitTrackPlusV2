// FitTrackPlus — Design System Components
// Tokens, icons, and shared UI primitives

const FT = {
  bg: '#F4F4F1',
  surface: '#FCFBF7',
  surfaceAlt: '#E8E5DD',
  surfaceCard: '#FFFFFF',
  border: '#D8D4CA',
  borderLight: '#ECEAE4',
  textPrimary: '#161816',
  textSecondary: '#5E655F',
  textTertiary: '#9AA09B',
  primary: '#1F6B57',
  primaryDark: '#174D40',
  primarySoft: '#D9E8E1',
  primaryMid: '#3A8870',
  accentWarm: '#C47A49',
  accentSoft: '#F1E2D6',
  error: '#B15249',
  errorSoft: '#F5E0DF',
  success: '#1F6B57',
  white: '#FFFFFF',
};

const FT_FONT = {
  sans: '"DM Sans", system-ui, sans-serif',
  mono: '"DM Mono", "SF Mono", monospace',
};

// ── SVG Icon Library ──────────────────────────────────────────
const Ico = {
  Home: ({size=22,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M3 9.5L12 3l9 6.5V20a1 1 0 01-1 1H5a1 1 0 01-1-1V9.5z" stroke={color} strokeWidth="1.6" strokeLinejoin="round"/>
      <path d="M9 21V12h6v9" stroke={color} strokeWidth="1.6" strokeLinejoin="round"/>
    </svg>
  ),
  Dumbbell: ({size=22,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <rect x="2" y="10" width="3" height="4" rx="1" fill={color}/>
      <rect x="5" y="8" width="2" height="8" rx="1" fill={color}/>
      <rect x="17" y="8" width="2" height="8" rx="1" fill={color}/>
      <rect x="19" y="10" width="3" height="4" rx="1" fill={color}/>
      <rect x="7" y="11" width="10" height="2" rx="1" fill={color}/>
    </svg>
  ),
  Play: ({size=22,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="9" stroke={color} strokeWidth="1.6"/>
      <path d="M10 8.5l6 3.5-6 3.5V8.5z" fill={color}/>
    </svg>
  ),
  History: ({size=22,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M12 8v4l2.5 2.5" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
      <path d="M3.5 12a8.5 8.5 0 108.5-8.5 8.5 8.5 0 00-8 5.5" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
      <path d="M3.5 7.5V12H8" stroke={color} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  Chart: ({size=22,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <rect x="3" y="13" width="4" height="8" rx="1" fill={color}/>
      <rect x="10" y="8" width="4" height="13" rx="1" fill={color}/>
      <rect x="17" y="4" width="4" height="17" rx="1" fill={color}/>
    </svg>
  ),
  Plus: ({size=20,color=FT.white}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M12 5v14M5 12h14" stroke={color} strokeWidth="2" strokeLinecap="round"/>
    </svg>
  ),
  ChevronLeft: ({size=20,color=FT.textPrimary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M15 18l-6-6 6-6" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  ChevronRight: ({size=18,color=FT.textTertiary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M9 18l6-6-6-6" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  Edit: ({size=18,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z" stroke={color} strokeWidth="1.6" strokeLinejoin="round"/>
    </svg>
  ),
  Archive: ({size=18,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <rect x="3" y="4" width="18" height="4" rx="1" stroke={color} strokeWidth="1.6"/>
      <path d="M5 8v11a1 1 0 001 1h12a1 1 0 001-1V8" stroke={color} strokeWidth="1.6"/>
      <path d="M10 12h4" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
    </svg>
  ),
  Check: ({size=18,color=FT.white}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M5 12l5 5L19 7" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  MoreVert: ({size=20,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="5" r="1.5" fill={color}/>
      <circle cx="12" cy="12" r="1.5" fill={color}/>
      <circle cx="12" cy="19" r="1.5" fill={color}/>
    </svg>
  ),
  Timer: ({size=18,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="13" r="8" stroke={color} strokeWidth="1.6"/>
      <path d="M12 9v4l2 2" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
      <path d="M9 3h6M12 3v2" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
    </svg>
  ),
  Trophy: ({size=18,color=FT.accentWarm}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M8 21h8M12 17v4M7 3h10v8a5 5 0 01-10 0V3z" stroke={color} strokeWidth="1.6" strokeLinejoin="round"/>
      <path d="M7 5H4a1 1 0 00-1 1v2a4 4 0 004 4M17 5h3a1 1 0 011 1v2a4 4 0 01-4 4" stroke={color} strokeWidth="1.6" strokeLinejoin="round"/>
    </svg>
  ),
  Flame: ({size=18,color=FT.accentWarm}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M12 2c0 4-4 5-4 9a4 4 0 008 0c0-4-4-5-4-9z" stroke={color} strokeWidth="1.6" strokeLinejoin="round"/>
      <path d="M12 12c0 2-1.5 2.5-1.5 4a1.5 1.5 0 003 0c0-1.5-1.5-2-1.5-4z" fill={color}/>
    </svg>
  ),
  Weight: ({size=18,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="7" r="4" stroke={color} strokeWidth="1.6"/>
      <path d="M4 21v-2a8 8 0 0116 0v2" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
    </svg>
  ),
  Close: ({size=20,color=FT.textSecondary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M18 6L6 18M6 6l12 12" stroke={color} strokeWidth="1.8" strokeLinecap="round"/>
    </svg>
  ),
  ArrowUp: ({size=16,color=FT.primary}) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M12 19V5M5 12l7-7 7 7" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
};

// ── Button ────────────────────────────────────────────────────
function FTButton({ label, variant='primary', icon, onClick, fullWidth, size='md', disabled }) {
  const [pressed, setPressed] = React.useState(false);

  const styles = {
    primary: { bg: FT.primary, color: '#fff', border: 'none', hoverBg: FT.primaryDark },
    secondary: { bg: 'transparent', color: FT.primary, border: `1.5px solid ${FT.primary}`, hoverBg: FT.primarySoft },
    ghost: { bg: 'transparent', color: FT.textSecondary, border: 'none', hoverBg: FT.surfaceAlt },
    destructive: { bg: FT.errorSoft, color: FT.error, border: 'none', hoverBg: '#EDD5D4' },
    warm: { bg: FT.accentSoft, color: FT.accentWarm, border: 'none', hoverBg: '#EDD5C6' },
  };
  const s = styles[variant] || styles.primary;
  const pad = size === 'sm' ? '8px 16px' : size === 'lg' ? '16px 28px' : '12px 22px';
  const fs = size === 'sm' ? 13 : size === 'lg' ? 16 : 14.5;

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      onMouseDown={() => setPressed(true)}
      onMouseUp={() => setPressed(false)}
      onMouseLeave={() => setPressed(false)}
      style={{
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
        padding: pad, borderRadius: 12, border: s.border || 'none',
        background: pressed ? s.hoverBg : s.bg,
        color: s.color, fontSize: fs, fontWeight: 600,
        fontFamily: FT_FONT.sans, cursor: disabled ? 'default' : 'pointer',
        width: fullWidth ? '100%' : 'auto',
        transition: 'all 0.12s ease',
        opacity: disabled ? 0.5 : 1,
        letterSpacing: '0.01em',
        userSelect: 'none',
        WebkitTapHighlightColor: 'transparent',
        outline: 'none',
        transform: pressed ? 'scale(0.98)' : 'scale(1)',
        boxShadow: variant === 'primary' && !disabled ? '0 2px 8px rgba(31,107,87,0.25)' : 'none',
      }}
    >
      {icon && <span style={{display:'flex',alignItems:'center'}}>{icon}</span>}
      {label}
    </button>
  );
}

// ── Card ──────────────────────────────────────────────────────
function FTCard({ children, style, onClick, padding='16px', noBorder }) {
  const [pressed, setPressed] = React.useState(false);
  return (
    <div
      onClick={onClick}
      onMouseDown={() => onClick && setPressed(true)}
      onMouseUp={() => onClick && setPressed(false)}
      onMouseLeave={() => onClick && setPressed(false)}
      style={{
        background: FT.surface,
        borderRadius: 16,
        border: noBorder ? 'none' : `1px solid ${FT.borderLight}`,
        padding,
        cursor: onClick ? 'pointer' : 'default',
        transition: 'all 0.12s ease',
        transform: pressed ? 'scale(0.99)' : 'scale(1)',
        ...style,
      }}
    >
      {children}
    </div>
  );
}

// ── Screen Header ─────────────────────────────────────────────
function FTHeader({ title, subtitle, onBack, right }) {
  return (
    <div style={{
      padding: '16px 20px 12px',
      background: FT.bg,
      display: 'flex', alignItems: 'center', gap: 12,
    }}>
      {onBack && (
        <button onClick={onBack} style={{
          background: FT.surface, border: `1px solid ${FT.borderLight}`,
          borderRadius: 10, width: 38, height: 38,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer', flexShrink: 0,
          outline: 'none',
        }}>
          <Ico.ChevronLeft />
        </button>
      )}
      <div style={{ flex: 1 }}>
        <div style={{
          fontSize: 22, fontWeight: 700, color: FT.textPrimary,
          fontFamily: FT_FONT.sans, letterSpacing: '-0.02em', lineHeight: 1.2,
        }}>{title}</div>
        {subtitle && (
          <div style={{
            fontSize: 13, color: FT.textSecondary, marginTop: 2,
            fontFamily: FT_FONT.sans,
          }}>{subtitle}</div>
        )}
      </div>
      {right && <div style={{flexShrink:0}}>{right}</div>}
    </div>
  );
}

// ── Bottom Navigation ─────────────────────────────────────────
function FTBottomNav({ activeTab, onTabChange }) {
  const tabs = [
    { id: 'inicio', label: 'Inicio', Icon: Ico.Home },
    { id: 'rutinas', label: 'Rutinas', Icon: Ico.Dumbbell },
    { id: 'entrenar', label: 'Entrenar', Icon: Ico.Play },
    { id: 'historial', label: 'Historial', Icon: Ico.History },
    { id: 'datos', label: 'Datos', Icon: Ico.Chart },
  ];
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      background: FT.surface,
      borderTop: `1px solid ${FT.borderLight}`,
      paddingBottom: 4,
    }}>
      {tabs.map(tab => {
        const active = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            style={{
              flex: 1, display: 'flex', flexDirection: 'column',
              alignItems: 'center', gap: 3,
              padding: '10px 0 6px',
              background: 'none', border: 'none', cursor: 'pointer',
              outline: 'none',
            }}
          >
            <tab.Icon
              size={22}
              color={active ? FT.primary : FT.textTertiary}
            />
            <span style={{
              fontSize: 10.5, fontWeight: active ? 600 : 400,
              color: active ? FT.primary : FT.textTertiary,
              fontFamily: FT_FONT.sans, letterSpacing: '0.01em',
            }}>{tab.label}</span>
          </button>
        );
      })}
    </div>
  );
}

// ── Chip ──────────────────────────────────────────────────────
function FTChip({ label, active, onClick, variant='default' }) {
  const bgMap = { default: active ? FT.primarySoft : FT.surfaceAlt, warm: active ? FT.accentSoft : FT.surfaceAlt };
  const colorMap = { default: active ? FT.primary : FT.textSecondary, warm: active ? FT.accentWarm : FT.textSecondary };
  return (
    <button onClick={onClick} style={{
      padding: '6px 14px', borderRadius: 999,
      background: bgMap[variant] || bgMap.default,
      color: colorMap[variant] || colorMap.default,
      border: active ? `1px solid ${variant==='warm'?FT.accentWarm:FT.primaryMid}22` : `1px solid ${FT.borderLight}`,
      fontSize: 13, fontWeight: active ? 600 : 400, fontFamily: FT_FONT.sans,
      cursor: 'pointer', outline: 'none',
      transition: 'all 0.12s',
      whiteSpace: 'nowrap',
    }}>{label}</button>
  );
}

// ── MetricBlock ───────────────────────────────────────────────
function FTMetric({ value, unit, label, accent, size='md' }) {
  const big = size === 'lg';
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 3 }}>
        <span style={{
          fontSize: big ? 32 : 22, fontWeight: 700,
          color: accent === 'warm' ? FT.accentWarm : accent === 'primary' ? FT.primary : FT.textPrimary,
          fontFamily: FT_FONT.mono, letterSpacing: '-0.02em', lineHeight: 1,
        }}>{value}</span>
        {unit && <span style={{
          fontSize: big ? 14 : 11, fontWeight: 500,
          color: FT.textSecondary, fontFamily: FT_FONT.sans,
        }}>{unit}</span>}
      </div>
      <span style={{
        fontSize: 11.5, color: FT.textTertiary, fontFamily: FT_FONT.sans,
        textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 500,
      }}>{label}</span>
    </div>
  );
}

// ── List Row ──────────────────────────────────────────────────
function FTRow({ label, sublabel, right, rightSub, onPress, icon, accent }) {
  const [pressed, setPressed] = React.useState(false);
  return (
    <div
      onClick={onPress}
      onMouseDown={() => onPress && setPressed(true)}
      onMouseUp={() => onPress && setPressed(false)}
      onMouseLeave={() => onPress && setPressed(false)}
      style={{
        display: 'flex', alignItems: 'center', gap: 12,
        padding: '13px 0',
        borderBottom: `1px solid ${FT.borderLight}`,
        cursor: onPress ? 'pointer' : 'default',
        background: pressed ? FT.surfaceAlt : 'transparent',
        transition: 'background 0.1s',
        borderRadius: 4,
      }}
    >
      {icon && (
        <div style={{
          width: 36, height: 36, borderRadius: 10,
          background: accent === 'warm' ? FT.accentSoft : FT.primarySoft,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0,
        }}>{icon}</div>
      )}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontSize: 15, fontWeight: 500, color: FT.textPrimary,
          fontFamily: FT_FONT.sans, lineHeight: 1.3,
        }}>{label}</div>
        {sublabel && <div style={{
          fontSize: 12.5, color: FT.textSecondary, marginTop: 2,
          fontFamily: FT_FONT.sans,
        }}>{sublabel}</div>}
      </div>
      {(right || rightSub) && (
        <div style={{ textAlign: 'right', flexShrink: 0 }}>
          {right && <div style={{
            fontSize: 14, fontWeight: 600, color: accent === 'warm' ? FT.accentWarm : FT.textPrimary,
            fontFamily: FT_FONT.mono,
          }}>{right}</div>}
          {rightSub && <div style={{
            fontSize: 12, color: FT.textTertiary, fontFamily: FT_FONT.sans,
          }}>{rightSub}</div>}
        </div>
      )}
      {onPress && <Ico.ChevronRight />}
    </div>
  );
}

// ── Empty State ───────────────────────────────────────────────
function FTEmpty({ icon, title, message, ctaLabel, onCta }) {
  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      justifyContent: 'center', padding: '48px 32px', gap: 12, textAlign: 'center',
    }}>
      <div style={{
        width: 64, height: 64, borderRadius: 20,
        background: FT.primarySoft,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        marginBottom: 4,
      }}>{icon}</div>
      <div style={{
        fontSize: 17, fontWeight: 700, color: FT.textPrimary,
        fontFamily: FT_FONT.sans,
      }}>{title}</div>
      <div style={{
        fontSize: 14, color: FT.textSecondary, lineHeight: 1.6,
        fontFamily: FT_FONT.sans, maxWidth: 240,
      }}>{message}</div>
      {ctaLabel && (
        <div style={{ marginTop: 8 }}>
          <FTButton label={ctaLabel} onClick={onCta} />
        </div>
      )}
    </div>
  );
}

// ── Section label ─────────────────────────────────────────────
function FTSection({ label, right }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      marginBottom: 10,
    }}>
      <span style={{
        fontSize: 11.5, fontWeight: 600, color: FT.textTertiary,
        fontFamily: FT_FONT.sans,
        textTransform: 'uppercase', letterSpacing: '0.08em',
      }}>{label}</span>
      {right && <span style={{
        fontSize: 12.5, fontWeight: 500, color: FT.primary,
        fontFamily: FT_FONT.sans, cursor: 'pointer',
      }}>{right}</span>}
    </div>
  );
}

// ── Progress bar ─────────────────────────────────────────────
function FTProgressBar({ value, max, color = FT.primary, height = 6 }) {
  const pct = Math.min(100, (value / max) * 100);
  return (
    <div style={{
      width: '100%', height, borderRadius: height/2,
      background: FT.surfaceAlt, overflow: 'hidden',
    }}>
      <div style={{
        width: `${pct}%`, height: '100%',
        background: color, borderRadius: height/2,
        transition: 'width 0.4s ease',
      }}/>
    </div>
  );
}

// ── TextField ────────────────────────────────────────────────
function FTTextField({ label, value, onChange, placeholder, type='text', hint }) {
  const [focused, setFocused] = React.useState(false);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      {label && <label style={{
        fontSize: 12, fontWeight: 600, color: FT.textSecondary,
        fontFamily: FT_FONT.sans, textTransform: 'uppercase', letterSpacing: '0.06em',
      }}>{label}</label>}
      <input
        type={type}
        value={value}
        onChange={e => onChange && onChange(e.target.value)}
        placeholder={placeholder}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        style={{
          width: '100%', padding: '12px 14px',
          borderRadius: 12, boxSizing: 'border-box',
          border: `1.5px solid ${focused ? FT.primary : FT.border}`,
          background: FT.surface,
          fontSize: 15, fontFamily: FT_FONT.sans, color: FT.textPrimary,
          outline: 'none',
          transition: 'border-color 0.15s',
        }}
      />
      {hint && <span style={{ fontSize: 12, color: FT.textTertiary, fontFamily: FT_FONT.sans }}>{hint}</span>}
    </div>
  );
}

// ── Badge ─────────────────────────────────────────────────────
function FTBadge({ label, variant='primary' }) {
  const variants = {
    primary: { bg: FT.primarySoft, color: FT.primary },
    warm: { bg: FT.accentSoft, color: FT.accentWarm },
    neutral: { bg: FT.surfaceAlt, color: FT.textSecondary },
    error: { bg: FT.errorSoft, color: FT.error },
    active: { bg: FT.primary, color: '#fff' },
  };
  const v = variants[variant] || variants.primary;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center',
      padding: '3px 10px', borderRadius: 999,
      background: v.bg, color: v.color,
      fontSize: 11.5, fontWeight: 600, fontFamily: FT_FONT.sans,
      letterSpacing: '0.02em',
    }}>{label}</span>
  );
}

// ── Divider ───────────────────────────────────────────────────
function FTDivider({ margin='0' }) {
  return <div style={{ height: 1, background: FT.borderLight, margin }} />;
}

// ── FAB ──────────────────────────────────────────────────────
function FTFAB({ onClick, icon }) {
  return (
    <button
      onClick={onClick}
      style={{
        position: 'absolute', bottom: 20, right: 20,
        width: 52, height: 52, borderRadius: 16,
        background: FT.primary, border: 'none',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        cursor: 'pointer', outline: 'none',
        boxShadow: '0 4px 16px rgba(31,107,87,0.35)',
        transition: 'transform 0.12s',
      }}
    >
      {icon || <Ico.Plus />}
    </button>
  );
}

// ── Mini bar chart ────────────────────────────────────────────
function FTBarChart({ data, maxVal, color = FT.primary, height = 64 }) {
  const max = maxVal || Math.max(...data.map(d => d.value));
  return (
    <div style={{
      display: 'flex', alignItems: 'flex-end', gap: 6,
      height, width: '100%',
    }}>
      {data.map((d, i) => (
        <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, height: '100%', justifyContent: 'flex-end' }}>
          <div style={{
            width: '100%', borderRadius: '4px 4px 0 0',
            background: d.highlight ? color : FT.surfaceAlt,
            height: `${Math.max(4, (d.value / max) * (height - 20))}px`,
            transition: 'height 0.3s ease',
          }}/>
          <span style={{
            fontSize: 10, color: d.highlight ? FT.textSecondary : FT.textTertiary,
            fontFamily: FT_FONT.sans, fontWeight: d.highlight ? 600 : 400,
          }}>{d.label}</span>
        </div>
      ))}
    </div>
  );
}

// Export all to window
Object.assign(window, {
  FT, FT_FONT, Ico,
  FTButton, FTCard, FTHeader, FTBottomNav, FTChip,
  FTMetric, FTRow, FTEmpty, FTSection, FTProgressBar,
  FTTextField, FTBadge, FTDivider, FTFAB, FTBarChart,
});
