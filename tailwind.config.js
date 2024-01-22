module.exports = {
  mode: "jit",
  content: [
    "./frontend/resources/**/*.html",
    process.env.NODE_ENV == "production"
      ? "./frontend/resources/public/js/main.js"
      : "./frontend/resources/public/js/cljs-runtime/*.js"
  ],
  theme: {
    extend: {}
  },
  variants: {},
  plugins: []
};
