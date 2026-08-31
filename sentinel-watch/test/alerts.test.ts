import assert from "node:assert/strict";
import test from "node:test";
import { findAlert } from "../src/alerts.js";

test("returns the documented Iron Man alert", () => {
  assert.deepEqual(findAlert(1), { heroId: 1, severity: "LOW", active: false });
});

test("returns undefined for an unknown hero", () => {
  assert.equal(findAlert(666), undefined);
});
