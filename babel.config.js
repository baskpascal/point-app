module.exports = function (api) {
  api.cache(true);
  return {
    presets: ['babel-preset-expo'],
    plugins: [
      [
        'module-resolver',
        {
          root: ['./'],
          alias: { '@': './app' },
        },
      ],
      // react-native-reanimated v4 ships its worklets plugin from
      // react-native-worklets. Keep it last in the plugins list.
      'react-native-worklets/plugin',
    ],
  };
};
