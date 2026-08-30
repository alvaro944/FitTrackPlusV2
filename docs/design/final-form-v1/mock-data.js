// FitTrackPlus — Centralized Mock Data
window.FTMockData = (function() {

const exercises = {
  benchPress: { id: 'bench_press', name: 'Bench Press', muscleGroup: 'Chest', unit: 'kg' },
  inclineDumbbell: { id: 'incline_db', name: 'Incline Dumbbell Press', muscleGroup: 'Chest', unit: 'kg' },
  cableFlye: { id: 'cable_flye', name: 'Cable Flye', muscleGroup: 'Chest', unit: 'kg' },
  overheadPress: { id: 'ohp', name: 'Overhead Press', muscleGroup: 'Shoulders', unit: 'kg' },
  lateralRaise: { id: 'lateral_raise', name: 'Lateral Raise', muscleGroup: 'Shoulders', unit: 'kg' },
  tricepPushdown: { id: 'tricep_pd', name: 'Tricep Pushdown', muscleGroup: 'Triceps', unit: 'kg' },
  squat: { id: 'squat', name: 'Back Squat', muscleGroup: 'Legs', unit: 'kg' },
  romanianDeadlift: { id: 'rdl', name: 'Romanian Deadlift', muscleGroup: 'Hamstrings', unit: 'kg' },
  legPress: { id: 'leg_press', name: 'Leg Press', muscleGroup: 'Legs', unit: 'kg' },
  pullUp: { id: 'pull_up', name: 'Pull-up', muscleGroup: 'Back', unit: 'bw' },
  bentOverRow: { id: 'bent_row', name: 'Bent Over Row', muscleGroup: 'Back', unit: 'kg' },
  latPulldown: { id: 'lat_pd', name: 'Lat Pulldown', muscleGroup: 'Back', unit: 'kg' },
  facePull: { id: 'face_pull', name: 'Face Pull', muscleGroup: 'Shoulders', unit: 'kg' },
  bicepCurl: { id: 'bicep_curl', name: 'Bicep Curl', muscleGroup: 'Biceps', unit: 'kg' },
  deadlift: { id: 'deadlift', name: 'Deadlift', muscleGroup: 'Back', unit: 'kg' },
  legExtension: { id: 'leg_ext', name: 'Leg Extension', muscleGroup: 'Legs', unit: 'kg' },
  calfRaise: { id: 'calf_raise', name: 'Calf Raise', muscleGroup: 'Legs', unit: 'kg' },
  hammerCurl: { id: 'hammer_curl', name: 'Hammer Curl', muscleGroup: 'Biceps', unit: 'kg' },
};

const routines = [
  {
    id: 'ppl_routine',
    name: 'Push Pull Legs',
    isActive: true,
    isArchived: false,
    createdAt: '2025-09-01',
    updatedAt: '2026-04-10',
    days: [
      {
        id: 'push_a', label: 'Push A', dayOfWeek: 'Lunes',
        exercises: [
          { exerciseId: 'bench_press', sets: 4, reps: '6-8', weight: 85, notes: 'Pausa 2s en pecho' },
          { exerciseId: 'incline_db', sets: 3, reps: '10-12', weight: 32, notes: '' },
          { exerciseId: 'ohp', sets: 3, reps: '8-10', weight: 50, notes: '' },
          { exerciseId: 'cable_flye', sets: 3, reps: '12-15', weight: 14, notes: 'Squeeze en contracción' },
          { exerciseId: 'lateral_raise', sets: 4, reps: '15-20', weight: 9, notes: '' },
          { exerciseId: 'tricep_pd', sets: 3, reps: '12-15', weight: 30, notes: '' },
        ]
      },
      {
        id: 'pull_a', label: 'Pull A', dayOfWeek: 'Martes',
        exercises: [
          { exerciseId: 'pull_up', sets: 4, reps: 'AMRAP', weight: 0, notes: 'Peso corporal' },
          { exerciseId: 'bent_row', sets: 4, reps: '6-8', weight: 70, notes: '' },
          { exerciseId: 'lat_pd', sets: 3, reps: '10-12', weight: 60, notes: '' },
          { exerciseId: 'face_pull', sets: 3, reps: '15-20', weight: 22, notes: '' },
          { exerciseId: 'bicep_curl', sets: 3, reps: '10-12', weight: 16, notes: '' },
          { exerciseId: 'hammer_curl', sets: 2, reps: '12-15', weight: 14, notes: '' },
        ]
      },
      {
        id: 'legs_a', label: 'Legs', dayOfWeek: 'Miércoles',
        exercises: [
          { exerciseId: 'squat', sets: 4, reps: '6-8', weight: 100, notes: '' },
          { exerciseId: 'rdl', sets: 3, reps: '8-10', weight: 80, notes: '' },
          { exerciseId: 'leg_press', sets: 3, reps: '10-12', weight: 150, notes: '' },
          { exerciseId: 'leg_ext', sets: 3, reps: '12-15', weight: 55, notes: '' },
          { exerciseId: 'calf_raise', sets: 4, reps: '15-20', weight: 60, notes: '' },
        ]
      },
      {
        id: 'push_b', label: 'Push B', dayOfWeek: 'Viernes',
        exercises: [
          { exerciseId: 'ohp', sets: 4, reps: '6-8', weight: 55, notes: '' },
          { exerciseId: 'incline_db', sets: 3, reps: '8-10', weight: 34, notes: '' },
          { exerciseId: 'bench_press', sets: 3, reps: '10-12', weight: 80, notes: 'Volumen' },
          { exerciseId: 'lateral_raise', sets: 4, reps: '15-20', weight: 10, notes: '' },
          { exerciseId: 'cable_flye', sets: 3, reps: '15-20', weight: 12, notes: '' },
          { exerciseId: 'tricep_pd', sets: 3, reps: '10-12', weight: 32, notes: '' },
        ]
      },
      {
        id: 'pull_b', label: 'Pull B', dayOfWeek: 'Sábado',
        exercises: [
          { exerciseId: 'deadlift', sets: 3, reps: '4-6', weight: 130, notes: 'RPE 8' },
          { exerciseId: 'bent_row', sets: 3, reps: '8-10', weight: 72.5, notes: '' },
          { exerciseId: 'lat_pd', sets: 3, reps: '10-12', weight: 62.5, notes: '' },
          { exerciseId: 'face_pull', sets: 3, reps: '15-20', weight: 24, notes: '' },
          { exerciseId: 'bicep_curl', sets: 3, reps: '8-10', weight: 18, notes: '' },
        ]
      },
    ]
  },
  {
    id: 'upper_lower',
    name: 'Upper Lower',
    isActive: false,
    isArchived: false,
    createdAt: '2025-07-15',
    updatedAt: '2025-11-20',
    days: [
      {
        id: 'upper_a', label: 'Upper A', dayOfWeek: 'Lunes',
        exercises: [
          { exerciseId: 'bench_press', sets: 4, reps: '6-8', weight: 87.5, notes: '' },
          { exerciseId: 'bent_row', sets: 4, reps: '6-8', weight: 72.5, notes: '' },
          { exerciseId: 'ohp', sets: 3, reps: '8-10', weight: 50, notes: '' },
          { exerciseId: 'lat_pd', sets: 3, reps: '10-12', weight: 62.5, notes: '' },
          { exerciseId: 'bicep_curl', sets: 3, reps: '10-12', weight: 18, notes: '' },
          { exerciseId: 'tricep_pd', sets: 3, reps: '10-12', weight: 32, notes: '' },
        ]
      },
      {
        id: 'lower_a', label: 'Lower A', dayOfWeek: 'Martes',
        exercises: [
          { exerciseId: 'squat', sets: 4, reps: '6-8', weight: 102.5, notes: '' },
          { exerciseId: 'rdl', sets: 3, reps: '8-10', weight: 82.5, notes: '' },
          { exerciseId: 'leg_press', sets: 3, reps: '10-12', weight: 155, notes: '' },
          { exerciseId: 'calf_raise', sets: 4, reps: '15-20', weight: 65, notes: '' },
        ]
      },
    ]
  },
  {
    id: 'full_body',
    name: 'Full Body',
    isActive: false,
    isArchived: true,
    createdAt: '2025-03-01',
    updatedAt: '2025-06-10',
    days: [
      {
        id: 'full_a', label: 'Full Body A', dayOfWeek: 'Lunes',
        exercises: [
          { exerciseId: 'squat', sets: 3, reps: '5', weight: 95, notes: '' },
          { exerciseId: 'bench_press', sets: 3, reps: '5', weight: 82.5, notes: '' },
          { exerciseId: 'bent_row', sets: 3, reps: '5', weight: 70, notes: '' },
        ]
      },
      {
        id: 'full_b', label: 'Full Body B', dayOfWeek: 'Miércoles',
        exercises: [
          { exerciseId: 'squat', sets: 3, reps: '5', weight: 97.5, notes: '' },
          { exerciseId: 'ohp', sets: 3, reps: '5', weight: 47.5, notes: '' },
          { exerciseId: 'deadlift', sets: 1, reps: '5', weight: 125, notes: '' },
        ]
      },
    ]
  }
];

// Generate realistic history sessions
function generateSessions() {
  const sessions = [];
  const today = new Date('2026-05-03');
  
  // Push A sessions
  const pushADates = [
    new Date('2026-04-28'), new Date('2026-04-14'), new Date('2026-03-31'),
    new Date('2026-03-17'), new Date('2026-03-03'), new Date('2026-02-17'),
    new Date('2026-02-03'), new Date('2026-01-20'), new Date('2026-01-06'),
  ];
  
  const pushAData = [
    { bench: [{w:85,r:8},{w:85,r:7},{w:85,r:7},{w:85,r:6}], incline:[{w:32,r:12},{w:32,r:11},{w:32,r:10}], ohp:[{w:50,r:10},{w:50,r:9},{w:50,r:9}], flye:[{w:14,r:15},{w:14,r:14},{w:14,r:13}], lat:[{w:9,r:20},{w:9,r:18},{w:9,r:17},{w:9,r:16}], tri:[{w:30,r:15},{w:30,r:14},{w:30,r:13}] },
    { bench: [{w:85,r:7},{w:85,r:7},{w:85,r:6},{w:82.5,r:7}], incline:[{w:32,r:11},{w:32,r:10},{w:30,r:12}], ohp:[{w:50,r:9},{w:50,r:8},{w:47.5,r:10}], flye:[{w:14,r:14},{w:14,r:13},{w:12,r:15}], lat:[{w:9,r:18},{w:9,r:17},{w:9,r:16},{w:8,r:18}], tri:[{w:30,r:14},{w:30,r:13},{w:28,r:14}] },
    { bench: [{w:82.5,r:8},{w:82.5,r:7},{w:82.5,r:7},{w:80,r:8}], incline:[{w:30,r:12},{w:30,r:11},{w:30,r:10}], ohp:[{w:47.5,r:10},{w:47.5,r:9},{w:47.5,r:8}], flye:[{w:12,r:15},{w:12,r:14},{w:12,r:13}], lat:[{w:8,r:20},{w:8,r:18},{w:8,r:17},{w:8,r:15}], tri:[{w:28,r:15},{w:28,r:14},{w:28,r:12}] },
    { bench: [{w:80,r:8},{w:80,r:8},{w:80,r:7},{w:80,r:6}], incline:[{w:30,r:12},{w:28,r:12},{w:28,r:11}], ohp:[{w:47.5,r:9},{w:45,r:10},{w:45,r:9}], flye:[{w:12,r:14},{w:12,r:13},{w:10,r:15}], lat:[{w:8,r:18},{w:8,r:17},{w:8,r:16},{w:8,r:15}], tri:[{w:28,r:14},{w:26,r:15},{w:26,r:14}] },
    { bench: [{w:80,r:7},{w:77.5,r:8},{w:77.5,r:7},{w:77.5,r:6}], incline:[{w:28,r:12},{w:28,r:10},{w:26,r:12}], ohp:[{w:45,r:10},{w:45,r:8},{w:42.5,r:10}], flye:[{w:10,r:15},{w:10,r:14},{w:10,r:12}], lat:[{w:8,r:17},{w:8,r:16},{w:7,r:18},{w:7,r:17}], tri:[{w:26,r:15},{w:26,r:14},{w:24,r:15}] },
    { bench: [{w:77.5,r:8},{w:77.5,r:7},{w:75,r:8},{w:75,r:7}], incline:[{w:26,r:12},{w:26,r:11},{w:26,r:10}], ohp:[{w:42.5,r:10},{w:42.5,r:9},{w:42.5,r:8}], flye:[{w:10,r:14},{w:10,r:12},{w:10,r:11}], lat:[{w:7,r:18},{w:7,r:16},{w:7,r:15},{w:7,r:14}], tri:[{w:24,r:15},{w:24,r:13},{w:22,r:15}] },
    { bench: [{w:75,r:8},{w:75,r:8},{w:75,r:7},{w:75,r:6}], incline:[{w:26,r:11},{w:24,r:12},{w:24,r:11}], ohp:[{w:42.5,r:8},{w:40,r:10},{w:40,r:9}], flye:[{w:10,r:12},{w:10,r:11},{w:10,r:10}], lat:[{w:7,r:16},{w:7,r:15},{w:7,r:14},{w:7,r:13}], tri:[{w:22,r:15},{w:22,r:14},{w:22,r:12}] },
    { bench: [{w:75,r:7},{w:72.5,r:8},{w:72.5,r:7},{w:72.5,r:6}], incline:[{w:24,r:12},{w:24,r:10},{w:22,r:12}], ohp:[{w:40,r:10},{w:40,r:8},{w:37.5,r:10}], flye:[{w:10,r:11},{w:10,r:10},{w:8,r:12}], lat:[{w:7,r:15},{w:7,r:14},{w:7,r:13},{w:6,r:16}], tri:[{w:22,r:14},{w:20,r:15},{w:20,r:14}] },
    { bench: [{w:72.5,r:8},{w:72.5,r:7},{w:70,r:8},{w:70,r:7}], incline:[{w:22,r:12},{w:22,r:11},{w:22,r:10}], ohp:[{w:37.5,r:10},{w:37.5,r:9},{w:37.5,r:8}], flye:[{w:8,r:14},{w:8,r:12},{w:8,r:11}], lat:[{w:6,r:18},{w:6,r:16},{w:6,r:15},{w:6,r:14}], tri:[{w:20,r:15},{w:20,r:13},{w:18,r:15}] },
  ];

  pushADates.forEach((date, i) => {
    const d = pushAData[i];
    const sets = [
      ...d.bench.map(s => ({ exerciseId: 'bench_press', weight: s.w, reps: s.r, completed: true, isPR: false, notes: '' })),
      ...d.incline.map(s => ({ exerciseId: 'incline_db', weight: s.w, reps: s.r, completed: true, isPR: false, notes: '' })),
      ...d.ohp.map(s => ({ exerciseId: 'ohp', weight: s.w, reps: s.r, completed: true, isPR: false, notes: '' })),
      ...d.flye.map(s => ({ exerciseId: 'cable_flye', weight: s.w, reps: s.r, completed: true, isPR: false, notes: '' })),
      ...d.lat.map(s => ({ exerciseId: 'lateral_raise', weight: s.w, reps: s.r, completed: true, isPR: false, notes: '' })),
      ...d.tri.map(s => ({ exerciseId: 'tricep_pd', weight: s.w, reps: s.r, completed: true, isPR: false, notes: '' })),
    ];
    // mark PRs for first session of each exercise
    if (i === 0) {
      sets[0].isPR = true; // bench PR
    }
    const volume = sets.reduce((a, s) => a + s.weight * s.reps, 0);
    const durations = [58, 62, 55, 64, 57, 53, 60, 67, 51];
    sessions.push({
      id: `push_a_${i}`,
      routineId: 'ppl_routine',
      dayId: 'push_a',
      dayLabel: 'Push A',
      routineName: 'Push Pull Legs',
      date: date.toISOString(),
      durationMinutes: durations[i],
      sets,
      volume,
      notes: i === 0 ? 'Buen entreno. Bench se sintió sólido.' : i === 2 ? 'Cansado al llegar, pero terminé bien.' : '',
      totalSets: sets.length,
    });
  });

  // Pull A sessions
  const pullADates = [
    new Date('2026-04-29'), new Date('2026-04-15'), new Date('2026-04-01'),
    new Date('2026-03-18'), new Date('2026-03-04'), new Date('2026-02-18'),
  ];
  pullADates.forEach((date, i) => {
    const pullW = [70, 70, 67.5, 67.5, 65, 65][i];
    const sets = [
      { exerciseId: 'pull_up', weight: 0, reps: [8,7,6,5][i % 4] + i, completed: true, isPR: false },
      { exerciseId: 'pull_up', weight: 0, reps: [7,6,6,5][i % 4] + i, completed: true, isPR: false },
      { exerciseId: 'pull_up', weight: 0, reps: [6,6,5,4][i % 4] + i, completed: true, isPR: false },
      { exerciseId: 'pull_up', weight: 0, reps: [6,5,5,4][i % 4] + i, completed: true, isPR: false },
      { exerciseId: 'bent_row', weight: pullW, reps: 8, completed: true, isPR: false },
      { exerciseId: 'bent_row', weight: pullW, reps: 7, completed: true, isPR: false },
      { exerciseId: 'bent_row', weight: pullW, reps: 7, completed: true, isPR: false },
      { exerciseId: 'bent_row', weight: pullW, reps: 6, completed: true, isPR: false },
      { exerciseId: 'lat_pd', weight: 60, reps: 12, completed: true, isPR: false },
      { exerciseId: 'lat_pd', weight: 60, reps: 11, completed: true, isPR: false },
      { exerciseId: 'lat_pd', weight: 60, reps: 10, completed: true, isPR: false },
      { exerciseId: 'face_pull', weight: 22, reps: 18, completed: true, isPR: false },
      { exerciseId: 'face_pull', weight: 22, reps: 16, completed: true, isPR: false },
      { exerciseId: 'face_pull', weight: 22, reps: 15, completed: true, isPR: false },
      { exerciseId: 'bicep_curl', weight: 16, reps: 12, completed: true, isPR: false },
      { exerciseId: 'bicep_curl', weight: 16, reps: 11, completed: true, isPR: false },
      { exerciseId: 'bicep_curl', weight: 16, reps: 10, completed: true, isPR: false },
    ];
    const volume = sets.reduce((a, s) => a + s.weight * s.reps, 0);
    sessions.push({
      id: `pull_a_${i}`, routineId: 'ppl_routine', dayId: 'pull_a', dayLabel: 'Pull A',
      routineName: 'Push Pull Legs', date: date.toISOString(),
      durationMinutes: [52, 55, 48, 60, 50, 53][i],
      sets, volume, notes: '', totalSets: sets.length,
    });
  });

  // Legs sessions
  const legsDates = [
    new Date('2026-04-30'), new Date('2026-04-16'), new Date('2026-04-02'),
    new Date('2026-03-19'), new Date('2026-03-05'),
  ];
  legsDates.forEach((date, i) => {
    const squatW = [100, 100, 97.5, 97.5, 95][i];
    const sets = [
      { exerciseId: 'squat', weight: squatW, reps: 8, completed: true, isPR: i === 0 },
      { exerciseId: 'squat', weight: squatW, reps: 7, completed: true, isPR: false },
      { exerciseId: 'squat', weight: squatW, reps: 7, completed: true, isPR: false },
      { exerciseId: 'squat', weight: squatW, reps: 6, completed: true, isPR: false },
      { exerciseId: 'rdl', weight: 80, reps: 10, completed: true, isPR: false },
      { exerciseId: 'rdl', weight: 80, reps: 9, completed: true, isPR: false },
      { exerciseId: 'rdl', weight: 80, reps: 8, completed: true, isPR: false },
      { exerciseId: 'leg_press', weight: 150, reps: 12, completed: true, isPR: false },
      { exerciseId: 'leg_press', weight: 150, reps: 11, completed: true, isPR: false },
      { exerciseId: 'leg_press', weight: 150, reps: 10, completed: true, isPR: false },
      { exerciseId: 'leg_ext', weight: 55, reps: 15, completed: true, isPR: false },
      { exerciseId: 'leg_ext', weight: 55, reps: 14, completed: true, isPR: false },
      { exerciseId: 'leg_ext', weight: 55, reps: 13, completed: true, isPR: false },
      { exerciseId: 'calf_raise', weight: 60, reps: 20, completed: true, isPR: false },
      { exerciseId: 'calf_raise', weight: 60, reps: 18, completed: true, isPR: false },
      { exerciseId: 'calf_raise', weight: 60, reps: 17, completed: true, isPR: false },
      { exerciseId: 'calf_raise', weight: 60, reps: 15, completed: true, isPR: false },
    ];
    const volume = sets.reduce((a, s) => a + s.weight * s.reps, 0);
    sessions.push({
      id: `legs_${i}`, routineId: 'ppl_routine', dayId: 'legs_a', dayLabel: 'Legs',
      routineName: 'Push Pull Legs', date: date.toISOString(),
      durationMinutes: [68, 72, 65, 70, 66][i],
      sets, volume, notes: i === 0 ? 'Squat se sintió pesado pero limpio.' : '', totalSets: sets.length,
    });
  });

  // Sort all sessions by date descending
  sessions.sort((a, b) => new Date(b.date) - new Date(a.date));
  return sessions;
}

const sessions = generateSessions();

// Compute best sets per exercise from history
function getBestSets() {
  const bests = {};
  sessions.forEach(session => {
    session.sets.forEach(set => {
      if (!bests[set.exerciseId] || set.weight * set.reps > bests[set.exerciseId].weight * bests[set.exerciseId].reps) {
        bests[set.exerciseId] = { ...set, date: session.date };
      }
    });
  });
  return bests;
}

// Generate heatmap data (last 365 days)
function getHeatmapData() {
  const map = {};
  sessions.forEach(s => {
    const key = s.date.slice(0, 10);
    map[key] = (map[key] || 0) + 1;
  });
  return map;
}

// Stats per exercise over time
function getExerciseStats(exerciseId) {
  const pts = [];
  sessions.forEach(s => {
    const exSets = s.sets.filter(set => set.exerciseId === exerciseId);
    if (exSets.length === 0) return;
    const maxWeight = Math.max(...exSets.map(x => x.weight));
    const maxReps = Math.max(...exSets.filter(x => x.weight === maxWeight).map(x => x.reps));
    const totalVol = exSets.reduce((a, x) => a + x.weight * x.reps, 0);
    const estimated1RM = maxWeight * (1 + maxReps / 30);
    pts.push({ date: s.date.slice(0, 10), maxWeight, maxReps, volume: totalVol, estimated1RM: Math.round(estimated1RM) });
  });
  return pts.sort((a, b) => a.date.localeCompare(b.date));
}

// Get sessions this week (Mon–Sun)
function getSessionsThisWeek() {
  const now = new Date('2026-05-03');
  const day = now.getDay(); // 0=Sun,1=Mon...
  const diff = day === 0 ? -6 : 1 - day;
  const startOfWeek = new Date(now);
  startOfWeek.setDate(now.getDate() + diff);
  startOfWeek.setHours(0, 0, 0, 0);
  const endOfWeek = new Date(startOfWeek);
  endOfWeek.setDate(startOfWeek.getDate() + 7);
  return sessions.filter(s => {
    const d = new Date(s.date);
    return d >= startOfWeek && d < endOfWeek;
  }).length;
}

// Next workout day
function getNextWorkoutDay() {
  const active = routines.find(r => r.isActive);
  if (!active) return null;
  const lastSession = sessions.find(s => s.routineId === active.id);
  if (!lastSession) return active.days[0];
  const lastDayIdx = active.days.findIndex(d => d.id === lastSession.dayId);
  return active.days[(lastDayIdx + 1) % active.days.length];
}

return {
  exercises,
  routines,
  sessions,
  getBestSets,
  getHeatmapData,
  getExerciseStats,
  getSessionsThisWeek,
  getNextWorkoutDay,
  appVersion: '1.0.0-beta.4',
};
})();
