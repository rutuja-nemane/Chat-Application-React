import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

function ChatComponent() {
  const [stompClient, setStompClient] = useState(null);
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/chat'),
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
      });
      

    client.onConnect = () => {
      console.log('Connected to WebSocket');
      setIsConnected(true); // Update connection state
      client.subscribe('/topic/messages', (msg) => {
        setMessages((prevMessages) => [
          ...prevMessages,
          { text: msg.body, sender: 'other' },
        ]);
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP Error:', frame);
    };

    client.activate();
    setStompClient(client);

    return () => client.deactivate(); // Cleanup on unmount
  }, []);

  const sendMessage = () => {
    if (isConnected && message.trim()) {
      stompClient.publish({ destination: '/app/chat', body: message });
      setMessages((prevMessages) => [...prevMessages, { text: message, sender: 'me' }]);
      setMessage(''); // Clear the input field
    } else {
      console.log('Cannot send message: WebSocket is not connected');
    }
  };

  
  return (
    <div>
      <div className="chat-window">
        {messages.map((msg, index) => (
          <div key={index} className={msg.sender === 'me' ? 'sent' : 'received'}>
            {msg.text}
          </div>
        ))}
      </div>
      <input
        type="text"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
      />
      <button onClick={sendMessage}>Send</button>
    </div>
  );
}

export default ChatComponent;
