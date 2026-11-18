// 앱과 같은 색으로 보이도록 ?theme=volt&mode=dark 를 받는다. 없으면 기기 설정을 따른다.
// <head>에서 바로 실행해 첫 화면부터 맞는 색으로 그린다.
(function () {
  var params = new URLSearchParams(location.search);
  var themes = ["volt", "sunrise", "river", "ink"];
  var theme = params.get("theme");
  var mode = params.get("mode");
  var root = document.documentElement;
  root.dataset.theme = themes.indexOf(theme) >= 0 ? theme : "volt";
  if (mode !== "light" && mode !== "dark") {
    mode = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }
  root.dataset.mode = mode;
})();
