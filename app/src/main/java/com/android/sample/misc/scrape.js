// Run the following code on https://www.epfl.ch/campus/associations/list/
// to extract the list of associations

const ul = document.querySelector("h3 + p + ul");

const associations = Array.from(ul.children).map((e) => {
  const text = e.textContent;
  const url = e.querySelector("a")?.href;

  if (text.includes("(")) {
    return {
      name: text.match(/.+?(?=\W\()/)[0],
      fullName: text.match(/(?<=\().+?(?=\))/)[0],
      url,
    };
  } else {
    return {
      name: text,
      fullName: text,
      url,
    };
  }
});

console.log(JSON.stringify(associations));
