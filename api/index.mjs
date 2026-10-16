import { provider } from "../server/provider.mjs";
import { createRequestHandler } from "../server/http.mjs";

const handler = createRequestHandler(provider, process.env.BRIDGE_TOKEN || "");

export default async function (req, res) {
  return handler(req, res);
}
