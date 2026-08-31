import { createServer } from "node:http";
import { findAlert } from "./alerts.js";

export function createApp() {
  return createServer((request, response) => {
    if (request.method === "GET" && request.url === "/health") {
      response.writeHead(200, { "content-type": "application/json" });
      response.end(JSON.stringify({ status: "UP" }));
      return;
    }

    const match = request.url?.match(/^\/alerts\/(\d+)$/);
    if (request.method === "GET" && match) {
      const heroId = Number(match[1]);
      const alert = findAlert(heroId);
      response.writeHead(alert ? 200 : 404, { "content-type": "application/json" });
      response.end(JSON.stringify(alert ?? { status: 404, message: "Hero not found" }));
      return;
    }

    response.writeHead(404, { "content-type": "application/json" });
    response.end(JSON.stringify({ status: 404, message: "Route not found" }));
  });
}

if (process.argv[1]?.endsWith("server.js")) {
  const port = Number(process.env.PORT ?? 8090);
  createApp().listen(port, "0.0.0.0", () => console.log(`Sentinel Watch listening on ${port}`));
}
