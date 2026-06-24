import React, {useLayoutEffect, useRef} from 'react';
import {setTopPosition} from "./ScrollPosition";

export function Paragraphs({paragraphs, scrollPosition, language}) {
  return paragraphs.map((paragraph) =>
    <Paragraph
      paragraph={paragraph}
      key={paragraph.index}
      scrollPosition={scrollPosition}
      language={language}/>
  )
}

function Paragraph({paragraph, scrollPosition, language}) {

  const ref = useRef(null);
  useLayoutEffect(() => {
    const topPosition = ref.current.offsetTop;
    setTopPosition(paragraph.index, topPosition);
    if (scrollPosition !== null && paragraph.index
      === scrollPosition.index) {
      document.getElementById('content-container').scrollTo({
        top: topPosition - scrollPosition.offset
      });
    }
  });

  return (<p ref={ref} lang={language} className={`par ${paragraph.type}`}>
    {paragraph.number && <span className="par-number">{paragraph.number}</span>} {paragraph.text}
  </p>);
}