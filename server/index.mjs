import os from "node:os";
import { provider } from "./provider.mjs";
import { createServer } from "./http.mjs";
const host =
  process.env.BRIDGE_HOST ||
  (process.argv.includes("--lan") ? "0.0.0.0" : "127.0.0.1");
const port = Number(process.env.PORT || 8787);
const server = createServer(provider, process.env.BRIDGE_TOKEN || "");
server.listen(port, host, () => {
  const isWildcard = host === "0.0.0.0" || host === "::";
  const displayHost = isWildcard ? "localhost" : (host.includes(":") ? `[${host}]` : host);
  console.log(`HongguoTV 服务运行中：http://${displayHost}:${port}`);
  console.log(`👉 网页版播放入口：http://${displayHost}:${port}/`);
  if (isWildcard) {
    for (const list of Object.values(os.networkInterfaces()))
      for (const nic of list || []) {
        if (!nic.internal) {
          const formatted = nic.family === "IPv6" ? `[${nic.address}]` : nic.address;
          console.log(`   访问入口：http://${formatted}:${port}/`);
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
