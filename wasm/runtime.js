async function run() {
  const out = document.getElementById("output");
  try {
    const response = await fetch("./module.wasm");
    if (!response.ok) {
      throw new Error(`HTTP ${response.status} while fetching module.wasm`);
    }

    const bytes = await response.arrayBuffer();
    const { instance } = await WebAssembly.instantiate(bytes, {});

    if (!instance.exports.Answer) {
      throw new Error("Exported function 'Answer' not found.");
    }

    const value = instance.exports.Answer();
    out.textContent = `Answer() => ${value}`;
    console.log("WASM Answer() =>", value);
  } catch (err) {
    out.textContent = `Runtime error: ${err.message}`;
    console.error(err);
  }
}

run();
