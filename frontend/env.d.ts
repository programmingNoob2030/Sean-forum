/// <reference types="vite/client" />

// 告诉 TS：看到 .vue 结尾的文件，按 Vue 组件处理
declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

// 告诉 TS：看到 .css 结尾的文件，别报错，它只是个样式
declare module "*.css" {
  const content: any;
  export default content;
}
/// <reference types="vite/client" />

// 这一段是告诉 TS：所有以 .vue 结尾的文件，都是一个标准的 Vue 组件
declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
interface ImportMetaEnv {
  readonly VITE_RESOURCE_URL: string
}
interface ImportMeta {
  readonly env: ImportMetaEnv
}