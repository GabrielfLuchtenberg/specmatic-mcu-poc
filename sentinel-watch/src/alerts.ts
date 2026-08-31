export type Severity = "LOW" | "HIGH" | "CRITICAL";

export interface Alert {
  heroId: number;
  severity: Severity;
  active: boolean;
}

const alerts = new Map<number, Alert>([
  [1, { heroId: 1, severity: "LOW", active: false }],
  [2, { heroId: 2, severity: "HIGH", active: true }],
]);

export function findAlert(heroId: number): Alert | undefined {
  return alerts.get(heroId);
}
