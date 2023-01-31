import {BookItem} from './BookItem';

export function AuthorBookItems({author, books}) {
  return (
      <div className="author-books">
        <h1>{author}</h1>
        {
          books.map(item =>
              <BookItem title={item.title} language={item.language}
                        id={item.id} key={item.id}/>
          )
        }
      </div>
  )
}