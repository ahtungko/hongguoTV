import os from "node:os";
import { provider } from "./provider.mjs";
import { createServer } from "./http.mjs";
const host =
  process.env.BRIDGE_HOST ||
  (process.argv.includes("--lan") ? "0.0.0.0" : "127.0.0.1");
const port = Number(process.env.PORT || 8787);
const server = createServer(provider, process.env.BRIDGE_TOKEN || "");
server.listen(port, host, () => {
  const displayHost = host === "0.0.0.0" ? "localhost" : host;
  console.log(`HongguoTV 服务运行中：http://${displayHost}:${port}`);
  console.log(`👉 网页版播放入口：http://${displayHost}:${port}/`);
  if (host === "0.0.0.0") {
    for (const list of Object.values(os.networkInterfaces()))
      for (const nic of list || []) {
        if (nic.family === "IPv4" && !nic.internal) {
          console.log(`   局域网访问：http://${nic.address}:${port}/`);
        }
      }
  }
  console.log(
    "服务用于家庭网络；/health 仅表示进程正常。真实内容状态请运行 npm run probe。"
  );
});
server.on("error", (error) => {
  console.error(error.code);
  process.exitCode = 1;
});
for (const signal of ["SIGINT", "SIGTERM"])
  process.on(signal, () => {
    server.close();
    server.closeAllConnections();
  });
