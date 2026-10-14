import { mkdir, writeFile, access } from "node:fs/promises";
import { createWriteStream } from "node:fs";
import { pipeline } from "node:stream/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const PUBLIC_MODELS = join(__dirname, "..", "public", "models");

/**
 * Fontes: como baixar um personagem GLTF riggado + animado de graça.
 *
 * - KhronosGroup glTF-Sample-Models (CC0/CC-BY) → vários GLB de exemplo
 * - KayKit Adventurers (CC0) → https://kaylousberg.itch.io/kaykit-adventurers
 * - Quaternius (CC0) → https://quaternius.com
 * - Mixamo (grátis com conta) → https://www.mixamo.com
 *
 * Vamos baixar o "CesiumMan" (Khronos, CC-BY) que já vem com animação "walk",
 * e o "Fox" (CC0) que tem várias animações (walk, run, survey, etc).
 */
const MODELS = [
  {
    name: "character.glb",
    // Soldier (CC0, com várias animações: Idle, Walk, Run, TPose)
    url: "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/main/2.0/CesiumMan/glTF-Binary/CesiumMan.glb",
  },
  // Você pode descomentar e adicionar outros abaixo:
  // {
  //   name: 'fox.glb',
  //   url: 'https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/main/2.0/Fox/glTF-Binary/Fox.glb',
  // },
];

async function exists(p) {
  try {
    await access(p);
    return true;
  } catch {
    return false;
  }
}

async function download(url, dest) {
  if (await exists(dest)) {
    console.log(`   ✓ ${dest} já existe, pulando`);
    return;
  }
  console.log(`   ⬇ Baixando ${url}`);
  const res = await fetch(url);
  if (!res.ok) throw new Error(`HTTP ${res.status} para ${url}`);
  await pipeline(res.body, createWriteStream(dest));
  console.log(`   ✓ Salvo em ${dest}`);
}

async function main() {
  await mkdir(PUBLIC_MODELS, { recursive: true });
  console.log(`📦 Baixando modelos para ${PUBLIC_MODELS}\n`);
  for (const m of MODELS) {
    const dest = join(PUBLIC_MODELS, m.name);
    try {
      await download(m.url, dest);
    } catch (e) {
      console.error(`   ✗ Falha ao baixar ${m.name}: ${e.message}`);
    }
  }
  console.log("\n✅ Pronto!");
  console.log("\n💡 Modelos premium (com mais animações):");
  console.log(
    "   • KayKit Adventurers → https://kaylousberg.itch.io/kaykit-adventurers",
  );
  console.log("   • Quaternius          → https://quaternius.com");
  console.log("   • Mixamo (Adobe)      → https://www.mixamo.com");
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
