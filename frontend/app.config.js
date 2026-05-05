import 'dotenv/config';

export default {
  expo: {
    name: "kevend",
    slug: "kevend",

    ios: {
      config: {
        googleMapsApiKey: process.env.AIzaSyCJtX8AoJp4vRstzRmDBnxGlOA_67meGdw,
      },
    },

    android: {
      config: {
        googleMaps: {
          apiKey: process.env.AIzaSyCJtX8AoJp4vRstzRmDBnxGlOA_67meGdw,
        },
      },
    },
  },
};