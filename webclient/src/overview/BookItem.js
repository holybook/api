export function BookItem({title, id, language}) {
  return (<a className="book-item" href={`/books/${id}?lang=${language}`}>
    {title}
  </a>)
}