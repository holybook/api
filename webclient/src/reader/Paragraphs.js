import React, {useLayoutEffect, useRef, useState} from 'react';
import {setTopPosition} from "./ScrollPosition";

export function Paragraphs({paragraphs, scrollPosition}) {
  return paragraphs.map((paragraph) =>
    <Paragraph
      paragraph={paragraph}
      key={paragraph.index}
      scrollPosition={scrollPosition}/>
  )
}

function Paragraph({paragraph, scrollPosition}) {

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

  return (<p ref={ref} className={`par ${paragraph.type}`}>
    {paragraph.number && <span className="par-number">{paragraph.number}</span>} {paragraph.text}
  </p>);
}