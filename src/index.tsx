// import { PackageConfigNotFound } from './error-handling/PackageConfig';
import Ota from './NativeOta';
import type { Config } from './types';

export function setConfig(config: Config) {
  return Ota.setConfig(config);
}

//TODO: adiciona link para uma breve descrição da config
// throw new PackageConfigNotFound(
//   'react-native-ota config not found add config in your package.json, see https://link.com'
// );
