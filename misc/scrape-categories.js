// Run the following code on https://www.epfl.ch/campus/associations/category/
// to extract the list of associations by category.

(() => {
  const categoriesHeaders = Array.from(document.querySelectorAll("#post-2124 > div > h3"));
  
  const categories = categoriesHeaders.flatMap((e) => {
    const category = e.textContent;
    return Array.from(e.nextElementSibling.children).map((e) => {
      const parts = e.textContent.split("–");
      const acronym = parts[0]?.trim();
      const name = parts[1]?.trim();
      const url = e.querySelector("a")?.href;
  
      return {
        acronym,
        name,
        url,
        category
      };
    });
  });
  
  console.log(JSON.stringify(categories));
})();