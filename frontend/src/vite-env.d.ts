/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_WS_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// Permite importar arquivos .css, .svg, .png etc
declare module '*.css';
declare module '*.scss';
declare module '*.svg' {
  const src: string;
  export default src;
}
declare module '*.png' {
  const src: string;
  export default src;
}