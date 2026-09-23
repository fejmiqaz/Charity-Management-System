import {useEffect} from "react"; export default function useBodyClass(name){useEffect(()=>{document.body.className=name;return()=>{document.body.className=''}},[name])}
