import {Component, OnInit} from '@angular/core';
import SwaggerUI from 'swagger-ui';

@Component({
  selector: 'app-documentation',
  templateUrl: './documentation.component.html',
  styleUrl: './documentation.component.css'
})
export class DocumentationComponent implements OnInit {
  ngOnInit(): void {
    SwaggerUI({
      url: '../../assets/openapi.yaml',
      domNode: document.getElementById('swagger-container')
    })
  }

}
