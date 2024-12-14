import {ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, Renderer2, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Message} from './message';
import {SharedChatService} from '../shared-chat.service';
import {ChatService} from '../../_service/chat.service';
import {SharedAppService} from '../../shared-app.service';
import {TokenStorageService} from '../../_service/token-storage.service';
import {asyncScheduler, observeOn} from 'rxjs';

declare const signalR: any;

@Component({
  selector: 'app-chat',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('chatContainer') private chatContainer!: ElementRef;

  private isChatVisible: boolean = false;

  newMessage: string = '';
  messages: Message[] = [];

  constructor(private sharedChatService: SharedChatService, private chatService: ChatService,
              private renderer: Renderer2, private sharedAppService: SharedAppService, private token: TokenStorageService,
              private cdRef: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.sharedChatService.initialMessages$.subscribe(m => {
      if (m != null){
        this.messages.push(new Message('bot', m.message1));
        this.messages.push(new Message('bot', m.message2));
        this.messages.push(new Message('bot', m.message3));
      }
    });
    this.sharedChatService.isChatVisible$.subscribe(c => this.isChatVisible = c);
    this.loadScripts();
  }

  ngOnDestroy(): void {
    this.sharedChatService.connection?.stop();
  }

  initSignalR(): void {
    if (this.sharedAppService.isLoggedIn){
      this.sharedChatService.connection = new signalR.HubConnectionBuilder()
        .withUrl(`https://hiking-alerts.org:8080/v1/chat`, {
          accessTokenFactory: () => this.token.getToken()
        })
        .withAutomaticReconnect()
        .configureLogging(signalR.LogLevel.Information)
        .build();
      this.sharedChatService.connection.on('newMessage', (m: any) => {
        Promise.resolve().then(() => {
          this.messages.push(new Message('bot', m));
          this.cdRef.detectChanges();
          setTimeout(() => this.scrollToBottom(), 10);
          if (!this.isChatVisible) {
            this.sharedChatService.incrementCounter();
          }
        });
      });

      this.sharedChatService.connection.start()
        .then(() => console.log("Started signalr connection"))
        .catch(console.error)
    } else {
      console.log("Login necessary to create SignalR connection.");
    }
  }

  loadScripts(){
    this.addScripts([
      'https://cdn.jsdelivr.net/npm/@microsoft/signalr@8.0.7/dist/browser/signalr.min.js'
    ]);
  }

  private addScripts(scriptUrls: string[]): void {
    let loadedScriptsCount = 0;
    scriptUrls.forEach((s) => {
      if (!this.isScriptLoaded(s)) {
        const script = this.renderer.createElement('script');
        script.type = 'text/javascript';
        script.src = s;
        script.async = true;
        script.defer = true;
        script.onload = () => {
          console.log("loaded signalr");
          loadedScriptsCount++;
          this.initSignalR();
        };

        script.onerror = () => {
          console.error(`Error loading script: ${s}`);
        };
        this.renderer.appendChild(document.body, script);
      } else {
        loadedScriptsCount++;
        if (loadedScriptsCount === scriptUrls.length) {
          this.initSignalR();
        }
      }
    });
  }

  private isScriptLoaded(src: string): boolean {
    return Array.from(document.getElementsByTagName('script')).some(
      script => script.src.includes(src)
    );
  }

  sendMessage(): void {
    if (this.newMessage.trim()) {
      this.messages.push({ sender: 'user', content: this.newMessage.trim() });
      setTimeout(() => this.scrollToBottom());

      setTimeout(() => {
        this.chatService.chat(this.newMessage.trim()).pipe(
          observeOn(asyncScheduler)
        ).subscribe((content: string) => {
          setTimeout(() => console.log("signalr message  processed"));
        }, (err: any) => {
          setTimeout(() => {
            console.log("received error " + err);
            this.messages.push({ sender: 'bot', content: this.messages[2].content });
            setTimeout(() => this.scrollToBottom());
          });
        });
        this.newMessage = '';
      }, 0);
    }
  }

  private scrollToBottom(): void {
    try {
      this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  closeChat(): void {
    this.sharedChatService.setChatVisibility(false);
  }
}
