export class Message {
  content: string;
  sender: string;

  constructor(sender: string, content: string){
    this.content = content;
    this.sender = sender;
  }
}
