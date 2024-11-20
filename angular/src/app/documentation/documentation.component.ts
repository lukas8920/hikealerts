import {Component, OnInit, Renderer2} from '@angular/core';
import SwaggerUI from 'swagger-ui';

@Component({
  selector: 'app-documentation',
  templateUrl: './documentation.component.html',
  styleUrl: './documentation.component.css'
})
export class DocumentationComponent implements OnInit {
  constructor(private renderer: Renderer2) {
  }

  ngOnInit(): void {
    this.loadSwaggerUICSS();
    SwaggerUI({
      url: '../../assets/openapi.yaml',
      domNode: document.getElementById('swagger-container')
    })
  }

  private loadSwaggerUICSS(): void {
    const link = this.renderer.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'assets/swagger-ui.css';
    this.renderer.appendChild(document.head, link);
  }
}
