export enum Strategy {
  MANUAL = 'manual',
  ON_APP_START = 'on-app-start',
  ON_APP_STATE_CHANGE = 'on-app-state-change',
}

export interface EnvironmentConfig {
  enabled?: boolean;
  strategy?: Strategy;
  deploymentKey: string;
  url: string;
}

export interface Environments {
  development: EnvironmentConfig;
  staging: EnvironmentConfig;
  production: EnvironmentConfig;
}

export interface Config {
  enabled: boolean;
  environments: Environments;
}
