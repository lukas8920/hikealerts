export class Event {
  id: string;
  title: string;
  description: string;
  url: string;
  publisher: string;
  create_date: string;
  lat: number;
  lng: number;

  constructor(id: string, title: string, description: string, publisher: string, create_date: string, lat: number, lng: number, url: string) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.publisher = publisher;
    this.create_date = create_date;
    this.lat = lat;
    this.lng = lng;
    this.url = url;
  }
}
